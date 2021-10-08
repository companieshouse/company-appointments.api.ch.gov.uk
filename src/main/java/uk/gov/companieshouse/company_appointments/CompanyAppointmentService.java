package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CompanyAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private CompanyAppointmentRepository companyAppointmentRepository;
    private AppointmentApiRepository appointmentApiRepository;
    private CompanyAppointmentMapper companyAppointmentMapper;

    @Autowired
    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
                                     AppointmentApiRepository appointmentApiRepository,
                                     CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.appointmentApiRepository = appointmentApiRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public CompanyAppointmentView fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentData> appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentData.orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber))));
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
