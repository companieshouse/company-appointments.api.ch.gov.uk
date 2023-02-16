package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Document
public class OfficerAppointmentsAggregate {

    @Field("total_results")
    private Integer totalResults;
    @Field("officer_appointments")
    private List<CompanyAppointmentData> officerAppointments;

    public OfficerAppointmentsAggregate(Integer totalResults, List<CompanyAppointmentData> officerAppointments) {
        this.totalResults = totalResults;
        this.officerAppointments = officerAppointments;
    }

    public OfficerAppointmentsAggregate() {
        this.officerAppointments = new ArrayList<>();
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public OfficerAppointmentsAggregate setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
        return this;
    }

    public List<CompanyAppointmentData> getOfficerAppointments() {
        return officerAppointments;
    }

    public OfficerAppointmentsAggregate setOfficerAppointments(List<CompanyAppointmentData> officerAppointments) {
        this.officerAppointments = officerAppointments;
        return this;
    }
}