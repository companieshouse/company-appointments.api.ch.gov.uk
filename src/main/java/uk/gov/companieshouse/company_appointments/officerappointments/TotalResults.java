package uk.gov.companieshouse.company_appointments.officerappointments;

public class TotalResults {

    private Long count;

    public TotalResults(Long count) {
        this.count = count;
    }

    public TotalResults() {
    }

    public Long getCount() {
        return count;
    }

    public TotalResults setCount(Long count) {
        this.count = count;
        return this;
    }
}
