package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentV2View;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentV2View.CompanyAppointmentV2ViewBuilder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CompanyAppointmentV2Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private CompanyAppointmentV2Repository companyAppointmentRepository;
    private AppointmentApiRepository appointmentApiRepository;

    @Autowired
    public CompanyAppointmentV2Service(CompanyAppointmentV2Repository companyAppointmentRepository,
                                       AppointmentApiRepository appointmentApiRepository) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.appointmentApiRepository = appointmentApiRepository;
    }

    public CompanyAppointmentV2View getAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<AppointmentApiEntity> appointmentData = companyAppointmentRepository.findByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));

        return appointmentData.map(app -> CompanyAppointmentV2ViewBuilder.view(app.getData()).build()).orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void insertAppointmentDelta(final AppointmentAPI appointmentApi) {
        if (isMostRecentDelta(appointmentApi)) {
            appointmentApiRepository.insertOrUpdate(appointmentApi);
        } else {
            logStaleIncomingDelta(appointmentApi);
        }
    }

    private void logStaleIncomingDelta(final AppointmentAPI appointmentApi) {
        final Optional<String> existingAppointmentDelta = appointmentApiRepository
                .findById(appointmentApi.getId())
                .map(AppointmentApiEntity::getDeltaAt);

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("incomingDeltaAt", appointmentApi.getDeltaAt());
        logInfo.put("existingDeltaAt", existingAppointmentDelta.orElse("No existing delta"));
        final String context = appointmentApi.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }

    private boolean isMostRecentDelta(final AppointmentAPI incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String deltaAt = incomingAppointment.getDeltaAt();

        final boolean isNotMostRecent = appointmentApiRepository
                .existsByIdAndDeltaAtGreaterThanEqual(id, deltaAt);

        return !isNotMostRecent;
    }
}
