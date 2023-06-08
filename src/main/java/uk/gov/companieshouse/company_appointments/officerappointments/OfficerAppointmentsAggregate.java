package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Document
class OfficerAppointmentsAggregate {

    @Field("total_results")
    private Integer totalResults;
    @Field("officer_appointments")
    private List<CompanyAppointmentData> officerAppointments;

    OfficerAppointmentsAggregate() {
        this.officerAppointments = new ArrayList<>();
    }

    Integer getTotalResults() {
        return totalResults;
    }

    OfficerAppointmentsAggregate setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
        return this;
    }

    List<CompanyAppointmentData> getOfficerAppointments() {
        return officerAppointments;
    }

    OfficerAppointmentsAggregate setOfficerAppointments(List<CompanyAppointmentData> officerAppointments) {
        this.officerAppointments = officerAppointments;
        return this;
    }
}
