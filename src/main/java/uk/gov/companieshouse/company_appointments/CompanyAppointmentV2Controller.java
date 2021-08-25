package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Controller
@RequestMapping(path = "/appointments/v2/company/{company_number}/appointments/{appointment_id}", produces = "application/json")
public class CompanyAppointmentV2Controller {

    private CompanyAppointmentService companyAppointmentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    @Autowired
    public CompanyAppointmentV2Controller(CompanyAppointmentService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    @GetMapping
    public ResponseEntity<CompanyAppointmentView> fetchAppointment(@PathVariable("company_number") String companyNumber, @PathVariable("appointment_id") String appointmentID) {
        try {
            return ResponseEntity.ok(companyAppointmentService.fetchAppointment(companyNumber, appointmentID));
        } catch (NotFoundException e) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity submitOfficerData(@RequestBody final AppointmentAPI companyAppointmentData) {

        try {
            companyAppointmentService.putAppointmentData(companyAppointmentData);
        } catch (Exception e) {
            throw e;
        }
        return ResponseEntity.ok().build();
    }

}
