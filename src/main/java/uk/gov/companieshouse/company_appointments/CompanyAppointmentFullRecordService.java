package uk.gov.companieshouse.company_appointments;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
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

    private CompanyAppointmentFullRecordRepository companyAppointmentRepository;

    private Clock clock;

    @Autowired
    public CompanyAppointmentFullRecordService(CompanyAppointmentFullRecordRepository companyAppointmentRepository, Clock clock) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.clock = clock;
    }

    public CompanyAppointmentFullRecordView getAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<DeltaAppointmentApiEntity> appointmentData = companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));

        return appointmentData.map(app -> CompanyAppointmentFullRecordView.Builder.view(app)
                        .build())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void insertAppointmentDelta(final FullRecordCompanyOfficerApi appointmentApi) throws ServiceUnavailableException {

        DeltaAppointmentTransformer deltaAppointmentTransformer = new DeltaAppointmentTransformer();
        DeltaAppointmentApi deltaAppointmentApi;
        try {
            deltaAppointmentApi = deltaAppointmentTransformer.transform(appointmentApi);
        } catch(FailedToTransformException e) {
            throw new ServiceUnavailableException(String.format("Failed to transform payload: %s", e.getMessage()));
        }

        InstantAPI instant = new InstantAPI(Instant.now(clock));
        Data officer = deltaAppointmentApi.getData();

        if (officer != null) {
            deltaAppointmentApi.setUpdatedAt(instant);
            deltaAppointmentApi.setEtag(GenerateEtagUtil.generateEtag());
        }

        Optional<DeltaAppointmentApiEntity> existingAppointment = getExistingDelta(deltaAppointmentApi);

        if (existingAppointment.isPresent()) {
            updateAppointment(deltaAppointmentApi, existingAppointment.get());
        } else {
            saveAppointment(deltaAppointmentApi, instant);
        }
    }

    public void deleteOfficer(String companyNumber, String appointmentId) throws NotFoundException {
        LOGGER.debug(String.format("Deleting appointment [%s] for company [%s]", appointmentId, companyNumber));
        Optional<DeltaAppointmentApiEntity> deleted = companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);

        if (!deleted.isPresent()) {
            throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentId, companyNumber));
        }
    }

    private void saveAppointment(DeltaAppointmentApi appointmentApi, InstantAPI instant) {
        appointmentApi.setCreated(instant);
        DeltaAppointmentApiEntity saveRecord = new DeltaAppointmentApiEntity(appointmentApi);
        companyAppointmentRepository.insertOrUpdate(saveRecord);
    }

    private void updateAppointment(DeltaAppointmentApi appointmentApi, DeltaAppointmentApiEntity existingAppointment) {

        if (isDeltaStale(appointmentApi.getDeltaAt(), existingAppointment.getDeltaAt())) {
            logStaleIncomingDelta(appointmentApi, existingAppointment.getDeltaAt());
        } else {
            saveAppointment(appointmentApi, existingAppointment.getCreated());
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

