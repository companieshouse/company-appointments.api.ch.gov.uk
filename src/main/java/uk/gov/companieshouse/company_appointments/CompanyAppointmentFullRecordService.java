package uk.gov.companieshouse.company_appointments;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerLinksAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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
        Optional<AppointmentApiEntity> appointmentData = companyAppointmentRepository.findById(appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));

        return appointmentData.map(app -> CompanyAppointmentFullRecordView.Builder.view(app.getData(), app.getSensitiveData())
                        .build())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
    }

    public void insertAppointmentDelta(final AppointmentAPI appointmentApi) {

        InstantAPI instant = new InstantAPI(Instant.now(clock));
        OfficerAPI officer = appointmentApi.getData();

        appointmentApi.setUpdated(instant);

        if (officer != null) {
            appointmentApi.getData().setUpdatedAt(instant.getAt());
            removeAdditionalProperties(officer);
            officer.setEtag(GenerateEtagUtil.generateEtag());
        }

        Optional<AppointmentApiEntity> existingAppointment = getExistingDelta(appointmentApi);

        if (existingAppointment.isPresent()) {
            updateAppointment(appointmentApi, existingAppointment.get());
        } else {
            saveAppointment(appointmentApi, instant);
        }
    }

    private void saveAppointment(AppointmentAPI appointmentApi, InstantAPI instant) {
        appointmentApi.setCreated(instant);
        companyAppointmentRepository.insertOrUpdate(appointmentApi);
    }

    private void updateAppointment(AppointmentAPI appointmentApi, AppointmentApiEntity existingAppointment) {

        if (isDeltaStale(appointmentApi.getDeltaAt(), existingAppointment.getDeltaAt())) {
            logStaleIncomingDelta(appointmentApi, existingAppointment.getDeltaAt());
        } else {
            saveAppointment(appointmentApi, existingAppointment.getCreated());
        }
    }

    private boolean isDeltaStale(final String incomingDelta, final String existingDelta) {

        return StringUtils.compare(incomingDelta, existingDelta) <= 0;
    }

    private Optional<AppointmentApiEntity> getExistingDelta(final AppointmentAPI incomingAppointment) {

        final String id = incomingAppointment.getId();

        return companyAppointmentRepository.findById(id);
    }

    private void logStaleIncomingDelta(final AppointmentAPI appointmentAPI, final String existingDelta) {

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("incomingDeltaAt", appointmentAPI.getDeltaAt());
        logInfo.put("existingDeltaAt", StringUtils.defaultString(existingDelta, "No existing delta"));
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }

    private static void removeAdditionalProperties(final Object object) {
        if (object instanceof OfficerAPI) {
            final OfficerAPI officer = (OfficerAPI) object;
            officer.setAdditionalProperties(null);

            removeAdditionalProperties(officer.getServiceAddress());
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
}

