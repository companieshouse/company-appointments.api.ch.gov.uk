package uk.gov.companieshouse.company_appointments.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/company/{company_number}", produces = "application/json")
public class CompanyAppointmentController {

    private final CompanyAppointmentService companyAppointmentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    public CompanyAppointmentController(CompanyAppointmentService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    @GetMapping(path = "/appointments/{appointment_id}")
    public ResponseEntity<CompanyAppointmentView> fetchAppointment(@PathVariable("company_number") String companyNumber, @PathVariable("appointment_id") String appointmentID) {
        try {
            return ResponseEntity.ok(companyAppointmentService.fetchAppointment(companyNumber, appointmentID));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/officers-test")
    public ResponseEntity<AllCompanyAppointmentsView> fetchAppointmentsForCompany(
            @PathVariable("company_number") String companyNumber,
            @RequestParam(required = false) String filter,
            @RequestParam(name = "order_by", required = false) String orderBy,
            @RequestParam(required = false, name = "start_index") Integer startIndex,
            @RequestParam(required = false, name = "items_per_page") Integer itemsPerPage,
            @RequestParam(required = false, name = "register_view") Boolean registerView,
            @RequestParam(required = false, name = "register_type") String registerType) {

        try {
            return ResponseEntity.ok(companyAppointmentService.fetchAppointmentsForCompany(companyNumber, filter, orderBy, startIndex, itemsPerPage, registerView, registerType));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (BadRequestException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PatchMapping(path = "/appointments")
    public ResponseEntity<Void> patchCompanyNameStatus(
            @PathVariable("company_number") String companyNumber,
            @RequestBody PatchAppointmentNameStatusApi requestBody) {
        try {
            companyAppointmentService.patchCompanyNameStatus(companyNumber,
                    requestBody.getCompanyName(), requestBody.getCompanyStatus());
            return ResponseEntity.ok()
                    .header(HttpHeaders.LOCATION, String.format("/company/%s/appointments", companyNumber))
                    .build();
        } catch (BadRequestException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PatchMapping(path = "/appointments/{appointment_id}")
    public ResponseEntity<Void> patchNewAppointmentCompanyNameStatus(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("appointment_id") String appointmentId,
            @Valid @RequestBody PatchAppointmentNameStatusApi requestBody) {
        try {
            companyAppointmentService.patchNewAppointmentCompanyNameStatus(companyNumber, appointmentId,
                    requestBody.getCompanyName(), requestBody.getCompanyStatus());
            return ResponseEntity.ok()
                    .header(HttpHeaders.LOCATION, String.format("/company/%s/appointments/%s", companyNumber, appointmentId))
                    .build();
        } catch (BadRequestException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
