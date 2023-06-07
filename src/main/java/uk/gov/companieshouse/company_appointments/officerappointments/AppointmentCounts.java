package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.core.mapping.Field;

public class AppointmentCounts {

    private Integer activeCount;
    @Field("inactive_count")
    private Integer inactiveCount;
    @Field("resigned_count")
    private Integer resignedCount;

    private Integer totalCount;

    public Integer getActiveCount() {
        return activeCount;
    }

    public AppointmentCounts activeCount(Integer activeCount) {
        this.activeCount = activeCount;
        return this;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public AppointmentCounts inactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    public Integer getResignedCount() {
        return resignedCount;
    }

    public AppointmentCounts resignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public AppointmentCounts totalCount(Integer totalCount) {
        this.totalCount = totalCount;
        return this;
    }
}
