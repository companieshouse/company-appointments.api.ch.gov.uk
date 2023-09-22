package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
class CompanyAppointmentDocumentIdAggregate {

    @Field("total_results")
    private Integer totalResults;
    @Field("officer_appointments")
    private List<CompanyAppointmentDocumentId> officerAppointments;
    @Field("inactive_count")
    private Integer inactiveCount;
    @Field("resigned_count")
    private Integer resignedCount;

    CompanyAppointmentDocumentIdAggregate() {
        this.officerAppointments = new ArrayList<>();
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public CompanyAppointmentDocumentIdAggregate totalResults(Integer totalResults) {
        this.totalResults = totalResults;
        return this;
    }

    public List<CompanyAppointmentDocumentId> getOfficerAppointments() {
        return officerAppointments;
    }

    public CompanyAppointmentDocumentIdAggregate documentIds(
            List<CompanyAppointmentDocumentId> officerAppointments) {
        this.officerAppointments = officerAppointments;
        return this;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public CompanyAppointmentDocumentIdAggregate inactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    public Integer getResignedCount() {
        return resignedCount;
    }

    public CompanyAppointmentDocumentIdAggregate resignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }
}
