package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.annotation.Id;

public class CompanyAppointmentDocumentId {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public CompanyAppointmentDocumentId id(String id) {
        this.id = id;
        return this;
    }
}
