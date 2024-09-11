package uk.gov.companieshouse.company_appointments.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
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
    private static final ObjectMapper NULL_CLEANING_OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(Include.NON_NULL);

    private final DeltaAppointmentTransformer deltaAppointmentTransformer;
    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final ResourceChangedApiService resourceChangedApiService;
    private final CompanyAppointmentMapper companyAppointmentMapper;
    private final Clock clock;

    public CompanyAppointmentFullRecordService(
            DeltaAppointmentTransformer deltaAppointmentTransformer,
            CompanyAppointmentRepository companyAppointmentRepository,
            ResourceChangedApiService resourceChangedApiService,
            CompanyAppointmentMapper companyAppointmentMapper, Clock clock) {
        this.deltaAppointmentTransformer = deltaAppointmentTransformer;
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.companyAppointmentMapper = companyAppointmentMapper;
        this.clock = clock;
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

    @Transactional
    public void deleteAppointmentDelta(String companyNumber, String appointmentId)
            throws NotFoundException, ServiceUnavailableException {
        LOGGER.info(String.format("Deleting appointment [%s] for company [%s]", appointmentId, companyNumber),
                DataMapHolder.getLogMap());
        try {
            Optional<CompanyAppointmentDocument> document = companyAppointmentRepository.readByCompanyNumberAndID(
                    companyNumber, appointmentId);
            if (document.isEmpty()) {
                throw new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentId, companyNumber));
            }
            companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);

            // Serialise and deserialise OfficerSummary an extra time to remove null fields
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(
                    companyAppointmentMapper.map(document.get()));
            Object officerObject = NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);

            resourceChangedApiService.invokeChsKafkaApi(new ResourceChangedRequest(DataMapHolder.getRequestId(),
                    companyNumber, appointmentId, officerObject, true));
            LOGGER.debug(String.format("ChsKafka api DELETED invoked updated successfully for company number: %s",
                    companyNumber), DataMapHolder.getLogMap());
        } catch (DataAccessException e) {
            LOGGER.error(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw new UncheckedIOException(e);
        }
    }

    private Optional<CompanyAppointmentDocument> getExistingDelta(
            final CompanyAppointmentDocument incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String companyNumber = incomingAppointment.getCompanyNumber();

        return companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, id);
    }

    private void updateDocument(CompanyAppointmentDocument document, CompanyAppointmentDocument existingDocument,
            DeltaTimestamp instant) throws ServiceUnavailableException {
        if (isDeltaStale(document.getDeltaAt(), existingDocument.getDeltaAt())) {
            logStaleIncomingDelta(document, existingDocument.getDeltaAt());
        } else {
            try {
                saveDocument(document, instant, existingDocument.getCreated());
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
        logInfo.put("existingDeltaAt", existingDelta.toString().isBlank() ? existingDelta.toString() : "No existing delta");
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }

    private void insertDocument(CompanyAppointmentDocument document, DeltaTimestamp instant)
            throws ServiceUnavailableException {
        try {
            saveDocument(document, instant, instant);
        } catch (ServiceUnavailableException e) {
            LOGGER.info("Call to Kafka API failed", DataMapHolder.getLogMap());
            throw e;
        }
    }

    private void saveDocument(CompanyAppointmentDocument document, DeltaTimestamp updatedAt, DeltaTimestamp createdAt) {
        document.updated(updatedAt);
        document.created(createdAt);

        companyAppointmentRepository.save(document);
        resourceChangedApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(DataMapHolder.getRequestId(), document.getCompanyNumber(),
                        document.getAppointmentId(), null, false));
        LOGGER.debug(String.format("ChsKafka api CHANGED invoked updated successfully for company number: %s",
                document.getCompanyNumber()), DataMapHolder.getLogMap());
    }
}

