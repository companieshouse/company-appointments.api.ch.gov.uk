package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

class AppointmentsCounts {

    private Integer activeCount;
    @Field("inactive_count")
    private Integer inactiveCount;
    @Field("resigned_count")
    private Integer resignedCount;

    Integer getActiveCount() {
        return activeCount;
    }

    AppointmentsCounts activeCount(Integer activeCount) {
        this.activeCount = activeCount;
        return this;
    }

    Integer getInactiveCount() {
        return inactiveCount;
    }

    AppointmentsCounts inactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    Integer getResignedCount() {
        return resignedCount;
    }

    AppointmentsCounts resignedCount(Integer resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppointmentsCounts that = (AppointmentsCounts) o;
        return Objects.equals(activeCount, that.activeCount) && Objects.equals(inactiveCount,
                that.inactiveCount) && Objects.equals(resignedCount, that.resignedCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeCount, inactiveCount, resignedCount);
    }
}
