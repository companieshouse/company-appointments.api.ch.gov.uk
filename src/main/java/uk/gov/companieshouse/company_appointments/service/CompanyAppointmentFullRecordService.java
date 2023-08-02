package uk.gov.companieshouse.company_appointments.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
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

    private final DeltaAppointmentTransformer deltaAppointmentTransformer;
    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final ResourceChangedApiService resourceChangedApiService;
    private final Clock clock;
    
    @Autowired
    public CompanyAppointmentFullRecordService(
            DeltaAppointmentTransformer deltaAppointmentTransformer,
            CompanyAppointmentRepository companyAppointmentRepository, ResourceChangedApiService resourceChangedApiService, Clock clock) {
        this.deltaAppointmentTransformer = deltaAppointmentTransformer;
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.clock = clock;
    }

    public CompanyAppointmentFullRecordView getAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentDocument> appointmentData = companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));

        return appointmentData.map(app -> CompanyAppointmentFullRecordView.Builder.view(app).build())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void upsertAppointmentDelta(String contextId, final FullRecordCompanyOfficerApi requestBody) throws ServiceUnavailableException, NotFoundException {
            CompanyAppointmentDocument companyAppointmentDocument;
            try {
                companyAppointmentDocument = deltaAppointmentTransformer.transform(requestBody);
            } catch (IllegalArgumentException ex) {
                throw new NotFoundException(String.format("Company profile not found for company number [%s]", requestBody.getExternalData().getCompanyNumber()));
            } catch (FailedToTransformException ex) {
                throw new ServiceUnavailableException(String.format("Failed to transform payload: %s", ex.getMessage()));
            }
            var instant = new DeltaTimestamp(Instant.now(clock));
            DeltaOfficerData officer = companyAppointmentDocument.getData();

            if (officer != null) {
                companyAppointmentDocument.setUpdated(instant);
            }
            try {
                Optional<CompanyAppointmentDocument> existingAppointment = getExistingDelta(companyAppointmentDocument);
                if (existingAppointment.isPresent()) {
                    updateAppointment(contextId, companyAppointmentDocument, existingAppointment.get());
                } else {
                    saveAppointment(contextId, companyAppointmentDocument, instant);
                }
            } catch (DataAccessException e) {
                throw new ServiceUnavailableException("Error connecting to MongoDB");
            } catch (IllegalArgumentException e) {
                throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
            }
    }

    public void deleteAppointmentDelta(String contextId, String companyNumber, String appointmentId) throws NotFoundException, ServiceUnavailableException {
        LOGGER.debug(String.format("Deleting appointment [%s] for company [%s]", appointmentId, companyNumber));
        try {
            Optional<CompanyAppointmentDocument> appointmentData = companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, appointmentId);
            if (appointmentData.isEmpty()) {
                throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentId, companyNumber));
            }

            resourceChangedApiService.invokeChsKafkaApi(new ResourceChangedRequest(contextId, companyNumber, appointmentId, appointmentData, true));
            LOGGER.debug(String.format("ChsKafka api DELETED invoked updated successfully for context id: %s and company number: %s",
                    contextId,
                    companyNumber));

            companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);
        } catch (DataAccessException e) {
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        } catch (IllegalArgumentException e) {
            throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
        }
    }

    private void saveAppointment(String contextId, CompanyAppointmentDocument document, DeltaTimestamp instant) throws ServiceUnavailableException {
        resourceChangedApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, document.getCompanyNumber(), document.getAppointmentId(), null, false));
        LOGGER.debug(String.format("ChsKafka api CHANGED invoked updated successfully for context id: %s and company number: %s",
                contextId,
                document.getCompanyNumber()));
        document.setCreated(instant);
        companyAppointmentRepository.insertOrUpdate(document);
    }

    private void updateAppointment(String contextId, CompanyAppointmentDocument document, CompanyAppointmentDocument existingAppointment) throws ServiceUnavailableException {

        if (isDeltaStale(document.getDeltaAt(), existingAppointment.getDeltaAt())) {
            logStaleIncomingDelta(document, existingAppointment.getDeltaAt());
        } else {
            saveAppointment(contextId, document, existingAppointment.getCreated());
        }
    }

    private boolean isDeltaStale(final Instant incomingDelta, final Instant existingDelta) {
        return !incomingDelta.isAfter(existingDelta);
    }

    private Optional<CompanyAppointmentDocument> getExistingDelta(final CompanyAppointmentDocument incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String companyNumber = incomingAppointment.getCompanyNumber();

        return companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, id);
    }

    private void logStaleIncomingDelta(final CompanyAppointmentDocument appointmentAPI, final Instant existingDelta) {

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("incomingDeltaAt", appointmentAPI.getDeltaAt().toString());
        logInfo.put("existingDeltaAt", StringUtils.defaultString(existingDelta.toString(), "No existing delta"));
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }
}

