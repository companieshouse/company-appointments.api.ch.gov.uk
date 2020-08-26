package uk.gov.companieshouse.company_appointments;

import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

public class CompanyAppointmentController {

    private CompanyAppointmentService companyAppointmentService;

    public CompanyAppointmentController(CompanyAppointmentService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    public ResponseEntity<CompanyAppointmentView> fetchAppointment(String companyNumber, String appointmentID) {
        return ResponseEntity.ok(companyAppointmentService.fetchAppointment(companyNumber, appointmentID));
    }

}
