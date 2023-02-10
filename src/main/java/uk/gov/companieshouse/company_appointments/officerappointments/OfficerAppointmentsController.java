package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.model.officerappointments.OfficerAppointmentsApi;
import uk.gov.companieshouse.logging.Logger;

@Controller
public class OfficerAppointmentsController {

    private final OfficerAppointmentsService service;
    private final Logger logger;

    public OfficerAppointmentsController(OfficerAppointmentsService service, Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    @GetMapping(path = "/officers/{officer_id}/appointments")
    public ResponseEntity<OfficerAppointmentsApi> getOfficerAppointments(@PathVariable("officer_id") String officerId,
            @RequestParam("filter") String filter,
            @RequestParam("start_index") Integer startIndex,
            @RequestParam("items_per_page") Integer itemsPerPage) {
        return service.getOfficerAppointments(new OfficerAppointmentsRequest(officerId, filter, startIndex, itemsPerPage))
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.error(String.format("No appointments found for officer id %s", officerId));
                    return ResponseEntity.notFound().build();
                });
    }
}
