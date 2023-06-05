package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.core.mapping.Field;

public class AppointmentCounts {

    @Field("inactive_count")
    Integer inactiveCount;
    @Field("resigned_count")
    Integer resignedCount;

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
