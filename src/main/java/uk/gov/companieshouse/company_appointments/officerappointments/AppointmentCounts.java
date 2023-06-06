package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.core.mapping.Field;

public class AppointmentCounts {

    private Integer activeCount;
    @Field("inactive_count")
    private Integer inactiveCount;
    @Field("resigned_count")
    private Integer resignedCount;

    public Integer getActiveCount() {
        return activeCount;
    }

    public AppointmentCounts setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
        return this;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public AppointmentCounts setInactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    public Integer getResignedCount() {
        return resignedCount;
    }

    public AppointmentCounts setResignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }
}
