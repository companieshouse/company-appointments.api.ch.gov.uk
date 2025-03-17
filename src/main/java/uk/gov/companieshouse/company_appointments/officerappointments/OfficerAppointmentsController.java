package uk.gov.companieshouse.company_appointments.officerappointments;

import static uk.gov.companieshouse.company_appointments.interceptor.AuthenticationHelperImpl.ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Controller
class OfficerAppointmentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);

    private final OfficerAppointmentsService service;

    OfficerAppointmentsController(OfficerAppointmentsService service) {
        this.service = service;
    }

    @GetMapping(path = "/officers/{officer_id}/appointments")
    public ResponseEntity<AppointmentList> getOfficerAppointments(
            @PathVariable("officer_id") String officerId,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "start_index", required = false) Integer startIndex,
            @RequestParam(value = "items_per_page", required = false) Integer itemsPerPage,
            @RequestHeader(value = ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER, required = false) String authPrivileges) {
        try {
            DataMapHolder.get()
                    .officerId(officerId);
            LOGGER.info("Fetching officer appointments", DataMapHolder.getLogMap());

            OfficerAppointmentsRequest request = OfficerAppointmentsRequest.builder()
                    .officerId(officerId)
                    .filter(filter)
                    .startIndex(startIndex)
                    .itemsPerPage(itemsPerPage)
                    .authPrivileges(authPrivileges)
                    .build();
            return service.getOfficerAppointments(request)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        LOGGER.info(String.format("No appointments found for officer ID %s", officerId),
                                DataMapHolder.getLogMap());
                        return ResponseEntity.notFound().build();
                    });
        } catch (BadRequestException ex) {
            LOGGER.info(String.format("Invalid filter parameter supplied: %s, officer ID %s", filter, officerId),
                    DataMapHolder.getLogMap());
            return ResponseEntity.badRequest().build();
        }
    }
}
