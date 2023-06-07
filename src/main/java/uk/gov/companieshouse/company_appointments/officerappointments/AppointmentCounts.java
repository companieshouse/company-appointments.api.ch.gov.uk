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

    public void setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public void setInactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
    }

    public Integer getResignedCount() {
        return resignedCount;
    }

    public void setResignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
    }
}
