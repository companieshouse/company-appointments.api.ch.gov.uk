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
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaTimestamp;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class CompanyAppointmentFullRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
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

    @Transactional
    public void upsertAppointmentDelta(final FullRecordCompanyOfficerApi requestBody)
            throws ServiceUnavailableException {
        CompanyAppointmentDocument companyAppointmentDocument;
        try {
            companyAppointmentDocument = deltaAppointmentTransformer.transform(requestBody);
        } catch (FailedToTransformException ex) {
            throw new ServiceUnavailableException(String.format("Failed to transform payload: %s", ex.getMessage()));
        }
        var instant = new DeltaTimestamp(Instant.now(clock));
        DeltaOfficerData officer = companyAppointmentDocument.getData();

        if (officer != null) {
            companyAppointmentDocument.updated(instant);
        }
        try {
            Optional<CompanyAppointmentDocument> existingAppointment = getExistingDelta(companyAppointmentDocument);
            if (existingAppointment.isPresent()) {
                updateAppointment(companyAppointmentDocument, existingAppointment.get());
            } else {
                saveAppointment(companyAppointmentDocument, instant);
            }
        } catch (DataAccessException e) {
            LOGGER.debug(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
        }
    }

    @Transactional
    public void deleteAppointmentDelta(String companyNumber, String appointmentId)
            throws NotFoundException, ServiceUnavailableException {
        LOGGER.debug(String.format("Deleting appointment [%s] for company [%s]", appointmentId, companyNumber),
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
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(companyAppointmentMapper.map(document.get()));
            Object officerObject = NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);

            resourceChangedApiService.invokeChsKafkaApi(new ResourceChangedRequest(DataMapHolder.getRequestId(),
                    companyNumber, appointmentId, officerObject, true));
            LOGGER.debug(String.format("ChsKafka api DELETED invoked updated successfully for company number: %s",
                    companyNumber), DataMapHolder.getLogMap());
        } catch (DataAccessException e) {
            LOGGER.debug(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
        } catch (JsonProcessingException e) {
            LOGGER.debug("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw new UncheckedIOException(e);
        }
    }

    private void saveAppointment(CompanyAppointmentDocument document, DeltaTimestamp instant)
            throws ServiceUnavailableException {
        document.created(instant);
        companyAppointmentRepository.insertOrUpdate(document);
        resourceChangedApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(DataMapHolder.getRequestId(), document.getCompanyNumber(),
                        document.getAppointmentId(), null, false));
        LOGGER.debug(String.format("ChsKafka api CHANGED invoked updated successfully for company number: %s",
                document.getCompanyNumber()), DataMapHolder.getLogMap());
    }

    private void updateAppointment(CompanyAppointmentDocument document, CompanyAppointmentDocument existingAppointment)
            throws ServiceUnavailableException {

        if (isDeltaStale(document.getDeltaAt(), existingAppointment.getDeltaAt())) {
            logStaleIncomingDelta(document, existingAppointment.getDeltaAt());
        } else {
            saveAppointment(document, existingAppointment.getCreated());
        }
    }

    private boolean isDeltaStale(final Instant incomingDelta, final Instant existingDelta) {
        return !incomingDelta.isAfter(existingDelta);
    }

    private Optional<CompanyAppointmentDocument> getExistingDelta(
            final CompanyAppointmentDocument incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String companyNumber = incomingAppointment.getCompanyNumber();

        return companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, id);
    }

    private void logStaleIncomingDelta(final CompanyAppointmentDocument appointmentAPI, final Instant existingDelta) {

        Map<String, Object> logInfo = DataMapHolder.getLogMap();
        logInfo.put("incomingDeltaAt", appointmentAPI.getDeltaAt().toString());
        logInfo.put("existingDeltaAt", StringUtils.defaultString(existingDelta.toString(), "No existing delta"));
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }
}

