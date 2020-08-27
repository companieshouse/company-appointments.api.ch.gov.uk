package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

@Controller
@RequestMapping("/company/{company_number}/appointments/{appointment_id}")
public class CompanyAppointmentController {

    private CompanyAppointmentService companyAppointmentService;

    @Autowired
    public CompanyAppointmentController(CompanyAppointmentService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    @GetMapping
    public ResponseEntity<CompanyAppointmentView> fetchAppointment(@PathVariable("company_number") String companyNumber, @PathVariable("appointment_id") String appointmentID) {
        return ResponseEntity.ok(companyAppointmentService.fetchAppointment(companyNumber, appointmentID));
    }

}
