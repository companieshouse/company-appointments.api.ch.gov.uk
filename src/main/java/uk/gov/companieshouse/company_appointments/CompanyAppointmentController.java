package uk.gov.companieshouse.company_appointments;

import org.springframework.http.ResponseEntity;

public class CompanyAppointmentController {

    private CompanyAppointmentService companyAppointmentService;

    public CompanyAppointmentController(CompanyAppointmentService companyAppointmentService) {
        this.companyAppointmentService = companyAppointmentService;
    }

    public ResponseEntity<CompanyAppointment> fetchAppointment(String companyNumber, String appointmentID) {
        return ResponseEntity.ok(companyAppointmentService.fetchAppointment(companyNumber, appointmentID));
    }

}
