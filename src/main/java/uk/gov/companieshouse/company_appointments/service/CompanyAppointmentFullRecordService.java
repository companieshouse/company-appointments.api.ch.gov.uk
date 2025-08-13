package uk.gov.companieshouse.company_appointments.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.ConflictException;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.kafka.OfficerMergeProducer;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaTimestamp;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class CompanyAppointmentFullRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);

    private final DeltaAppointmentTransformer deltaAppointmentTransformer;
    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final ResourceChangedApiService resourceChangedApiService;
    private final Clock clock;
    private final OfficerMergeProducer officerMergeProducer;

    public CompanyAppointmentFullRecordService(
            DeltaAppointmentTransformer deltaAppointmentTransformer,
            CompanyAppointmentRepository companyAppointmentRepository,
            ResourceChangedApiService resourceChangedApiService,
            Clock clock, OfficerMergeProducer officerMergeProducer) {
        this.deltaAppointmentTransformer = deltaAppointmentTransformer;
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.clock = clock;
        this.officerMergeProducer = officerMergeProducer;
    }

    public CompanyAppointmentFullRecordView getAppointment(String companyNumber, String appointmentID)
            throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber),
                DataMapHolder.getLogMap());
        Optional<CompanyAppointmentDocument> appointmentData = companyAppointmentRepository.readByCompanyNumberAndID(
                companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(
                String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber),
                DataMapHolder.getLogMap()));

        return appointmentData.map(app -> CompanyAppointmentFullRecordView.Builder.view(app).build())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void upsertAppointmentDelta(final FullRecordCompanyOfficerApi requestBody)
            throws ServiceUnavailableException {
        CompanyAppointmentDocument appointmentDocument;
        try {
            appointmentDocument = deltaAppointmentTransformer.transform(requestBody);
        } catch (FailedToTransformException ex) {
            LOGGER.error("Failed to transform payload");
            throw new ServiceUnavailableException(String.format("Failed to transform payload: %s", ex.getMessage()));
        }

        DeltaTimestamp instant = new DeltaTimestamp(Instant.now(clock));

        try {
            getExistingDelta(appointmentDocument).ifPresentOrElse(
                    existingDocument -> updateDocument(appointmentDocument, existingDocument, instant),
                    () -> insertDocument(appointmentDocument, instant));
        } catch (DataAccessException e) {
            LOGGER.error(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
        }
    }

    private Optional<CompanyAppointmentDocument> getExistingDelta(
            final CompanyAppointmentDocument incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String companyNumber = incomingAppointment.getCompanyNumber();

        return companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, id);
    }

    private void updateDocument(CompanyAppointmentDocument document, CompanyAppointmentDocument existingDocument,
            DeltaTimestamp instant) throws ServiceUnavailableException, ConflictException {
        if (isDeltaStale(document.getDeltaAt(), existingDocument.getDeltaAt())) {
            logStaleIncomingDelta(document, existingDocument.getDeltaAt());
            throw new ConflictException("Received stale delta");
        } else {
            String previousOfficerId = null;
            if (!document.getOfficerId().equals(existingDocument.getOfficerId()) &&
                    !isBlank(existingDocument.getOfficerId())) {
                previousOfficerId = existingDocument.getOfficerId();
            } else if (!document.getOfficerId().equals(document.getPreviousOfficerId()) &&
                    !isBlank(document.getPreviousOfficerId()) &&
                    !"vuIAhYYbRDhqzx9b3e_jd6Uhres".equals(document.getPreviousOfficerId())) {
                previousOfficerId = document.getPreviousOfficerId();
            }

            try {
                saveDocument(document, instant, existingDocument.getCreated(), previousOfficerId);
            } catch (ServiceUnavailableException e) {
                LOGGER.info("Call to Kafka API failed", DataMapHolder.getLogMap());
                throw e;
            }
        }
    }

    private boolean isDeltaStale(final Instant incomingDelta, final Instant existingDelta) {
        return incomingDelta.isBefore(existingDelta);
    }

    private void logStaleIncomingDelta(final CompanyAppointmentDocument appointmentAPI, final Instant existingDelta) {

        Map<String, Object> logInfo = DataMapHolder.getLogMap();
        logInfo.put("incomingDeltaAt", appointmentAPI.getDeltaAt().toString());
        logInfo.put("existingDeltaAt",
                isBlank(existingDelta.toString()) ? "No existing delta" : existingDelta.toString());
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }

    private void insertDocument(CompanyAppointmentDocument document, DeltaTimestamp instant)
            throws ServiceUnavailableException {
        try {
            saveDocument(document, instant, instant, null);
        } catch (ServiceUnavailableException e) {
            LOGGER.info("Call to Kafka API failed", DataMapHolder.getLogMap());
            throw e;
        }
    }

    private void saveDocument(CompanyAppointmentDocument document, DeltaTimestamp updatedAt, DeltaTimestamp createdAt,
            @Nullable String previousOfficerId) {
        document.updated(updatedAt);
        document.created(createdAt);

        companyAppointmentRepository.save(document);

        if (!isBlank(previousOfficerId)) {
            officerMergeProducer.invokeOfficerMerge(document.getOfficerId(), previousOfficerId);
        }

        resourceChangedApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(document.getCompanyNumber(),
                        document.getAppointmentId(), null, false));
        LOGGER.debug(String.format("ChsKafka api CHANGED invoked updated successfully for company number: %s",
                document.getCompanyNumber()), DataMapHolder.getLogMap());
    }
}

