package uk.gov.companieshouse.company_appointments.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaTimestamp;
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

    private final DeltaAppointmentTransformer deltaAppointmentTransformer;
    private final CompanyAppointmentFullRecordRepository companyAppointmentRepository;
    private final Clock clock;

    @Autowired
    public CompanyAppointmentFullRecordService(
            DeltaAppointmentTransformer deltaAppointmentTransformer,
            CompanyAppointmentFullRecordRepository companyAppointmentRepository, Clock clock) {
        this.deltaAppointmentTransformer = deltaAppointmentTransformer;
        this.companyAppointmentRepository = companyAppointmentRepository;
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

    public void insertAppointmentDelta(final FullRecordCompanyOfficerApi appointmentApi) throws ServiceUnavailableException {

        CompanyAppointmentDocument companyAppointmentDocument;
        try {
            companyAppointmentDocument = deltaAppointmentTransformer.transform(appointmentApi);
        } catch(FailedToTransformException e) {
            throw new ServiceUnavailableException(String.format("Failed to transform payload: %s", e.getMessage()));
        }

        DeltaTimestamp instant = new DeltaTimestamp(Instant.now(clock));
        DeltaOfficerData officer = companyAppointmentDocument.getData();

        if (officer != null) {
            companyAppointmentDocument.setUpdated(instant);
            companyAppointmentDocument.setEtag(GenerateEtagUtil.generateEtag());
        }

        Optional<CompanyAppointmentDocument> existingAppointment = getExistingDelta(companyAppointmentDocument);

        if (existingAppointment.isPresent()) {
            updateAppointment(companyAppointmentDocument, existingAppointment.get());
        } else {
            saveAppointment(companyAppointmentDocument, instant);
        }
    }

    public void deleteOfficer(String companyNumber, String appointmentId) throws NotFoundException {
        LOGGER.debug(String.format("Deleting appointment [%s] for company [%s]", appointmentId, companyNumber));
        Optional<CompanyAppointmentDocument> deleted = companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);

        if (deleted.isEmpty()) {
            throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentId, companyNumber));
        }
    }

    private void saveAppointment(CompanyAppointmentDocument document, DeltaTimestamp instant) {
        document.setCreated(instant);
        companyAppointmentRepository.insertOrUpdate(document);
    }

    private void updateAppointment(CompanyAppointmentDocument appointmentApi, CompanyAppointmentDocument existingAppointment) {

        if (isDeltaStale(appointmentApi.getDeltaAt(), existingAppointment.getDeltaAt())) {
            logStaleIncomingDelta(appointmentApi, existingAppointment.getDeltaAt());
        } else {
            saveAppointment(appointmentApi, existingAppointment.getCreated());
        }
    }

    private boolean isDeltaStale(final String incomingDelta, final String existingDelta) {
        return StringUtils.compare(incomingDelta, existingDelta) <= 0;
    }

    private Optional<CompanyAppointmentDocument> getExistingDelta(final CompanyAppointmentDocument incomingAppointment) {

        final String id = incomingAppointment.getId();
        final String companyNumber = incomingAppointment.getCompanyNumber();

        return companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, id);
    }

    private void logStaleIncomingDelta(final CompanyAppointmentDocument appointmentAPI, final String existingDelta) {

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("incomingDeltaAt", appointmentAPI.getDeltaAt());
        logInfo.put("existingDeltaAt", StringUtils.defaultString(existingDelta, "No existing delta"));
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }
}

