package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Document
class OfficerAppointmentsAggregate {

    @Field("total_results")
    private Integer totalResults;
    @Field("officer_appointments")
    private List<CompanyAppointmentDocument> officerAppointments;
    @Field("inactive_count")
    private Integer inactiveCount;
    @Field("resigned_count")
    private Integer resignedCount;


    OfficerAppointmentsAggregate() {
        this.officerAppointments = new ArrayList<>();
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public OfficerAppointmentsAggregate totalResults(Integer totalResults) {
        this.totalResults = totalResults;
        return this;
    }

    public List<CompanyAppointmentDocument> getOfficerAppointments() {
        return officerAppointments;
    }

    public OfficerAppointmentsAggregate officerAppointments(
            List<CompanyAppointmentDocument> officerAppointments) {
        this.officerAppointments = officerAppointments;
        return this;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public OfficerAppointmentsAggregate inactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    public Integer getResignedCount() {
        return resignedCount;
    }

    public OfficerAppointmentsAggregate resignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }
}
