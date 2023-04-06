package uk.gov.companieshouse.company_appointments.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CompanyAppointmentFullRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private final CompanyAppointmentFullRecordRepository companyAppointmentRepository;

    private final ResourceChangedApiService resourceChangedApiService;

    private final Clock clock;

    @Autowired
    public CompanyAppointmentFullRecordService(CompanyAppointmentFullRecordRepository companyAppointmentRepository, ResourceChangedApiService resourceChangedApiService, Clock clock) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.clock = clock;
    }

    public CompanyAppointmentFullRecordView getAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<DeltaAppointmentApiEntity> appointmentData = companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));

        return appointmentData.map(app -> CompanyAppointmentFullRecordView.Builder.view(app).build())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void upsertAppointmentDelta(String contextId, final FullRecordCompanyOfficerApi requestBody) throws ServiceUnavailableException {

            // TODO should one big try catch or two try catch blocks be used?
            DeltaAppointmentTransformer deltaAppointmentTransformer = new DeltaAppointmentTransformer();
            DeltaAppointmentApi deltaAppointmentApi;
            try {
                deltaAppointmentApi = deltaAppointmentTransformer.transform(requestBody);
            } catch (FailedToTransformException e) {
                throw new ServiceUnavailableException(String.format("Failed to transform payload: %s", e.getMessage()));
            }
            InstantAPI instant = new InstantAPI(Instant.now(clock));
            Data officer = deltaAppointmentApi.getData();

            if (officer != null) {
                deltaAppointmentApi.setUpdatedAt(instant);
                deltaAppointmentApi.setEtag(GenerateEtagUtil.generateEtag());
            }
            try {
                Optional<DeltaAppointmentApiEntity> existingAppointment = getExistingDelta(deltaAppointmentApi);
                if (existingAppointment.isPresent()) {
                    updateAppointment(contextId, deltaAppointmentApi, existingAppointment.get());
                } else {
                    saveAppointment(contextId, deltaAppointmentApi, instant);
                }
            } catch (DataAccessException e) {
                throw new ServiceUnavailableException("Error connecting to MongoDB");
            }
    }

    public void deleteAppointmentDelta(String contextId, String companyNumber, String appointmentId) throws NotFoundException, ServiceUnavailableException {
        LOGGER.debug(String.format("Deleting appointment [%s] for company [%s]", appointmentId, companyNumber));
        try {
            Optional<DeltaAppointmentApiEntity> appointmentData = companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, appointmentId);
            if (appointmentData.isEmpty()) {
                throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentId, companyNumber));
            }

            resourceChangedApiService.invokeChsKafkaApi(new ResourceChangedRequest(contextId, companyNumber, appointmentId, appointmentData, true));
            LOGGER.info(String.format("ChsKafka api DELETED invoked updated successfully for context id: %s and company number: %s",
                    contextId,
                    companyNumber));

            companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);
        } catch (DataAccessException e) {
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        }
    }

    private void saveAppointment(String contextId, DeltaAppointmentApi appointmentApi, InstantAPI instant) throws ServiceUnavailableException {
        resourceChangedApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, appointmentApi.getCompanyNumber(), appointmentApi.getAppointmentId(), null, false));
        LOGGER.info(String.format("ChsKafka api CHANGED invoked updated successfully for context id: %s and company number: %s",
                contextId,
                appointmentApi.getCompanyNumber()));
        appointmentApi.setCreated(instant);
        DeltaAppointmentApiEntity saveRecord = new DeltaAppointmentApiEntity(appointmentApi);
        companyAppointmentRepository.insertOrUpdate(saveRecord);
    }

    private void updateAppointment(String contextId, DeltaAppointmentApi appointmentApi, DeltaAppointmentApiEntity existingAppointment) throws ServiceUnavailableException {

        if (isDeltaStale(appointmentApi.getDeltaAt(), existingAppointment.getDeltaAt())) {
            logStaleIncomingDelta(appointmentApi, existingAppointment.getDeltaAt());
        } else {
            saveAppointment(contextId, appointmentApi, existingAppointment.getCreated());
        }
    }

    private boolean isDeltaStale(final String incomingDelta, final String existingDelta) {
        return StringUtils.compare(incomingDelta, existingDelta) <= 0;
    }

    private Optional<DeltaAppointmentApiEntity> getExistingDelta(final DeltaAppointmentApi incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String companyNumber = incomingAppointment.getCompanyNumber();

        return companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, id);
    }

    private void logStaleIncomingDelta(final DeltaAppointmentApi appointmentAPI, final String existingDelta) {

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("incomingDeltaAt", appointmentAPI.getDeltaAt());
        logInfo.put("existingDeltaAt", StringUtils.defaultString(existingDelta, "No existing delta"));
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }
}

