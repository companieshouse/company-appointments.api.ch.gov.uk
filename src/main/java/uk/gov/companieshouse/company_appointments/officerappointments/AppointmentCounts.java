package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.core.mapping.Field;

class AppointmentCounts {

    private Integer activeCount;
    @Field("inactive_count")
    private Integer inactiveCount;
    @Field("resigned_count")
    private Integer resignedCount;

    private Integer totalCount;

    Integer getActiveCount() {
        return activeCount;
    }

    AppointmentCounts activeCount(Integer activeCount) {
        this.activeCount = activeCount;
        return this;
    }

    Integer getInactiveCount() {
        return inactiveCount;
    }

    AppointmentCounts inactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    Integer getResignedCount() {
        return resignedCount;
    }

    AppointmentCounts resignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }

    Integer getTotalCount() {
        return totalCount;
    }

    AppointmentCounts totalCount(Integer totalCount) {
        this.totalCount = totalCount;
        return this;
    }
}
