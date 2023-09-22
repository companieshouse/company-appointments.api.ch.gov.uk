package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Document
class OfficerAppointmentsAggregate {

    @Field("officer_appointments")
    private List<CompanyAppointmentDocument> officerAppointments = new ArrayList<>();

    public List<CompanyAppointmentDocument> getOfficerAppointments() {
        return officerAppointments;
    }

    public OfficerAppointmentsAggregate officerAppointments(
            List<CompanyAppointmentDocument> officerAppointments) {
        this.officerAppointments = officerAppointments;
        return this;
    }
}
