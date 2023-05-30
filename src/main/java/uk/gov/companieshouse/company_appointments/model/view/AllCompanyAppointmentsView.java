package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllCompanyAppointmentsView {

    @JsonProperty("total_results")
    private int totalResults;

    @JsonProperty("items")
    private List<CompanyAppointmentView> items;

    @JsonProperty("active_count")
    private int activeCount;

    @JsonProperty("inactive_count")
    private int inactiveCount;

    @JsonProperty("resigned_count")
    private int resignedCount;


    public AllCompanyAppointmentsView(int totalResults, List<CompanyAppointmentView> items, int activeCount, int inactiveCount, int resignedCount) {
        this.totalResults = totalResults;
        this.items = items;
        this.activeCount = activeCount;
        this.inactiveCount = inactiveCount;
        this.resignedCount = resignedCount;

    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<CompanyAppointmentView> getItems() {
        return items;
    }

    public void setItems(List<CompanyAppointmentView> items) {
        this.items = items;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getInactiveCount() {
        return inactiveCount;
    }

    public void setInactiveCount(int inactiveCount) {
        this.inactiveCount = inactiveCount;
    }

    public int getResignedCount() {
        return resignedCount;
    }

    public void setResignedCount(int resignedCount) {
        this.resignedCount = resignedCount;
    }
}
