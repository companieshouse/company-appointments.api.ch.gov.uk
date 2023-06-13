package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.logging.Logger;

@Controller
class OfficerAppointmentsController {

    private final OfficerAppointmentsService service;
    private final Logger logger;

    OfficerAppointmentsController(OfficerAppointmentsService service, Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    @GetMapping(path = "/officers/{officer_id}/appointments")
    ResponseEntity<AppointmentList> getOfficerAppointments(@PathVariable("officer_id") String officerId,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "start_index", required = false) Integer startIndex,
            @RequestParam(value = "items_per_page", required = false) Integer itemsPerPage) {
        try {
            return service.getOfficerAppointments(new OfficerAppointmentsRequest(officerId, filter, startIndex, itemsPerPage))
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.error(String.format("No appointments found for officer ID %s", officerId));
                        return ResponseEntity.notFound().build();
                    });
        } catch (BadRequestException ex) {
            logger.error(String.format("Invalid filter parameter supplied: %s, officer ID %s", filter, officerId));
            return ResponseEntity.badRequest().build();
        }
    }
}
