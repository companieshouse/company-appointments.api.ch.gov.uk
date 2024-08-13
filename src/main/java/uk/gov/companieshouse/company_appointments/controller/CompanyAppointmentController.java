package uk.gov.companieshouse.company_appointments.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
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
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Controller
@RequestMapping(path = "/company/{company_number}", produces = "application/json")
public class CompanyAppointmentController {

    private final CompanyAppointmentService companyAppointmentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);

    public CompanyAppointmentController(CompanyAppointmentService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    @GetMapping(path = "/appointments/{appointment_id}")
    public ResponseEntity<OfficerSummary> fetchAppointment(@PathVariable("company_number") String companyNumber, @PathVariable("appointment_id") String appointmentID) {

        DataMapHolder.get()
                .companyNumber(companyNumber);
        LOGGER.info("Fetching appointment %s".formatted(appointmentID), DataMapHolder.getLogMap());

        try {
            return ResponseEntity.ok(companyAppointmentService.fetchAppointment(companyNumber, appointmentID));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/officers")
    public ResponseEntity<OfficerList> fetchAppointmentsForCompany(
            @PathVariable("company_number") String companyNumber,
            @RequestParam(required = false) String filter,
            @RequestParam(name = "order_by", required = false) String orderBy,
            @RequestParam(required = false, name = "start_index") Integer startIndex,
            @RequestParam(required = false, name = "items_per_page") Integer itemsPerPage,
            @RequestParam(required = false, name = "register_view") Boolean registerView,
            @RequestParam(required = false, name = "register_type") String registerType) {

        DataMapHolder.get()
                .companyNumber(companyNumber);
        LOGGER.info("Fetching company appointments", DataMapHolder.getLogMap());

        FetchAppointmentsRequest request = FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(companyNumber)
                        .withFilter(filter)
                        .withOrderBy(orderBy)
                        .withStartIndex(startIndex)
                        .withItemsPerPage(itemsPerPage)
                        .withRegisterView(registerView)
                        .withRegisterType(registerType)
                        .build();
        try {
            return ResponseEntity.ok(companyAppointmentService.fetchAppointmentsForCompany(request));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.notFound().build();
        } catch (BadRequestException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.badRequest().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PatchMapping(path = "/appointments")
    public ResponseEntity<Void> patchCompanyNameStatus(
            @PathVariable("company_number") String companyNumber,
            @RequestBody PatchAppointmentNameStatusApi requestBody) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .companyName(isBlank(requestBody.getCompanyName())? null: requestBody.getCompanyName())
                .companyStatus(isBlank(requestBody.getCompanyStatus())? null: List.of(requestBody.getCompanyStatus()));
        LOGGER.info("Patching company name and status", DataMapHolder.getLogMap());

        try {
            companyAppointmentService.patchCompanyNameStatus(companyNumber,
                    requestBody.getCompanyName(), requestBody.getCompanyStatus());
            return ResponseEntity.ok()
                    .header(HttpHeaders.LOCATION, String.format("/company/%s/officers", companyNumber))
                    .build();
        } catch (BadRequestException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.notFound().build();
        } catch (ServiceUnavailableException e) {
            LOGGER.info(e.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
