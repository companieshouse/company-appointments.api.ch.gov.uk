package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerLinksAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CompanyAppointmentFullRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private CompanyAppointmentFullRecordRepository companyAppointmentRepository;
    private AppointmentApiRepository appointmentApiRepository;

    @Autowired
    public CompanyAppointmentFullRecordService(CompanyAppointmentFullRecordRepository companyAppointmentRepository,
            AppointmentApiRepository appointmentApiRepository) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.appointmentApiRepository = appointmentApiRepository;
    }

    public CompanyAppointmentFullRecordView getAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<AppointmentApiEntity> appointmentData = companyAppointmentRepository.findByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));

        return appointmentData.map(app -> CompanyAppointmentFullRecordView.Builder.view(app.getData())
                        .build())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void insertAppointmentDelta(final AppointmentAPI appointmentApi) {
        if (isMostRecentDelta(appointmentApi)) {
            removeAdditionalProperties(appointmentApi);
            addCreatedAt(appointmentApi);
            appointmentApiRepository.insertOrUpdate(appointmentApi);
        } else {
            logStaleIncomingDelta(appointmentApi);
        }
    }

    private boolean isMostRecentDelta(final AppointmentAPI incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String deltaAt = incomingAppointment.getDeltaAt();

        final boolean isNotMostRecent = appointmentApiRepository.existsByIdAndDeltaAtGreaterThanEqual(id, deltaAt);

        return !isNotMostRecent;
    }

    private void logStaleIncomingDelta(final AppointmentAPI appointmentApi) {
        final Optional<String> existingAppointmentDelta = appointmentApiRepository.findById(appointmentApi.getId())
                .map(AppointmentApiEntity::getDeltaAt);

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("incomingDeltaAt", appointmentApi.getDeltaAt());
        logInfo.put("existingDeltaAt", existingAppointmentDelta.orElse("No existing delta"));
        final String context = appointmentApi.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }

    private static void removeAdditionalProperties(final AppointmentAPI appointment) {
        final OfficerAPI officer = appointment.getData();
        if (officer == null) {
            return;
        }

        removeAdditionalProperties(officer);
    }

    private static void removeAdditionalProperties(final Object object) {
        if (object instanceof OfficerAPI) {
            final OfficerAPI officer = (OfficerAPI) object;
            officer.setAdditionalProperties(null);

            removeAdditionalProperties(officer.getServiceAddress());
            removeAdditionalProperties(officer.getUsualResidentialAddress());
            removeAdditionalProperties(officer.getFormerNameData());
            removeAdditionalProperties(officer.getIdentificationData());
            removeAdditionalProperties(officer.getLinksData());
        }
        else if (object instanceof AddressAPI) {
            ((AddressAPI) object).setAdditionalProperties(null);
        }
        else if (object instanceof FormerNamesAPI) {
            ((FormerNamesAPI) object).setAdditionalProperties(null);
        }
        else if (object instanceof IdentificationAPI) {
            ((IdentificationAPI) object).setAdditionalProperties(null);
        }
        else if (object instanceof LinksAPI) {
            final LinksAPI links = (LinksAPI) object;
            links.setAdditionalProperties(null);
            removeAdditionalProperties(links.getOfficerLinksData());
        }
        else if (object instanceof OfficerLinksAPI) {
            ((OfficerLinksAPI) object).setAdditionalProperties(null);
        }
        else if (object instanceof List) {
            List<?> l = (List<?>) object;
            l.forEach(CompanyAppointmentFullRecordService::removeAdditionalProperties);
        }
    }

    private static void addCreatedAt(final AppointmentAPI appointment) {

        appointment.setCreatedAt(Instant.now());
    }
}
