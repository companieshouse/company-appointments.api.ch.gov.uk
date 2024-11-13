package uk.gov.companieshouse.company_appointments.controller;

import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.DeleteAppointmentParameters;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;
import uk.gov.companieshouse.company_appointments.service.DeleteAppointmentService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Controller
@RequestMapping(path = "/company/{company_number}/appointments/{appointment_id}/full_record", produces = "application/json")
public class CompanyAppointmentFullRecordController {

    private final CompanyAppointmentFullRecordService companyAppointmentService;
    private final DeleteAppointmentService deleteAppointmentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);

    public CompanyAppointmentFullRecordController(CompanyAppointmentFullRecordService companyAppointmentService,
            DeleteAppointmentService deleteAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
        this.deleteAppointmentService = deleteAppointmentService;
    }

    @GetMapping
    public ResponseEntity<CompanyAppointmentFullRecordView> getAppointment(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("appointment_id") String appointmentID) {

        DataMapHolder.get()
                .companyNumber(companyNumber);
        LOGGER.info("Fetching full records of company appointment %s".formatted(appointmentID),
                DataMapHolder.getLogMap());

        try {
            return ResponseEntity.ok(companyAppointmentService.getAppointment(companyNumber, appointmentID));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity<Void> submitOfficerData(
           @Valid @RequestBody final FullRecordCompanyOfficerApi companyAppointmentData) {
        try {
            DataMapHolder.get()
                    .companyNumber(extractCompanyNumber(companyAppointmentData))
                    .officerId(companyAppointmentData.getExternalData().getOfficerId())
                    .internalId(companyAppointmentData.getExternalData().getInternalId());
            LOGGER.info("Updating full records of company appointment %s"
                            .formatted(companyAppointmentData.getExternalData().getAppointmentId()),
                    DataMapHolder.getLogMap());

            companyAppointmentService.upsertAppointmentDelta(companyAppointmentData);
            return ResponseEntity.ok().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.error(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (IllegalArgumentException ex) {
            LOGGER.info(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping(path = "/{officer_id}")
    public ResponseEntity<Void> deleteOfficerData(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("appointment_id") String appointmentId,
            @PathVariable("officer_id") String officerId,
            @RequestHeader("X-DELTA-AT") String deltaAt) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .appointmentId(appointmentId)
                .officerId(officerId);
        LOGGER.info("Processing Company Appointment DELETE", DataMapHolder.getLogMap());

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(companyNumber)
                .appointmentId(appointmentId)
                .deltaAt(deltaAt)
                .officerId(officerId)
                .build();

        deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);
        return ResponseEntity.ok().build();
    }

    private static String extractCompanyNumber(FullRecordCompanyOfficerApi companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getExternalData())
                .map(ext -> Optional.ofNullable(ext.getCompanyNumber())
                        .orElseThrow(() -> new IllegalArgumentException("Missing company number")))
                .orElseThrow(() -> new IllegalArgumentException("Missing external data block"));
    }
}
