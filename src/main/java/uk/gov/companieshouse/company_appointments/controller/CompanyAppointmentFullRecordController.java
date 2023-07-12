package uk.gov.companieshouse.company_appointments.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Controller
@RequestMapping(path = "/company/{company_number}/appointments/{appointment_id}/full_record", produces = "application/json")
public class CompanyAppointmentFullRecordController {

    private final CompanyAppointmentFullRecordService companyAppointmentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    @Autowired
    public CompanyAppointmentFullRecordController(CompanyAppointmentFullRecordService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    @GetMapping
    public ResponseEntity<CompanyAppointmentFullRecordView> getAppointment(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("appointment_id") String appointmentID) {
        try {
            return ResponseEntity.ok(companyAppointmentService.getAppointment(companyNumber, appointmentID));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity<Void> submitOfficerData(
            @RequestHeader("x-request-id") String contextId,
            @Valid @RequestBody final FullRecordCompanyOfficerApi companyAppointmentData) {
        try {
            companyAppointmentService.upsertAppointmentDelta(contextId, companyAppointmentData);
            return ResponseEntity.ok().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (NotFoundException ex) {
            LOGGER.info(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping(path = "/delete")
    public ResponseEntity<Void> deleteOfficerData(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber,
            @PathVariable("appointment_id") String appointmentId) {
        try {
            companyAppointmentService.deleteAppointmentDelta(contextId, companyNumber, appointmentId);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

}
