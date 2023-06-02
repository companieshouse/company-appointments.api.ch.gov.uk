package uk.gov.companieshouse.company_appointments.officerappointments;

public class AppointmentCounts {

    int activeCount;
    int inactiveCount;
    int resignedCount;
    int totalCount;

    public int getActiveCount() {
        return activeCount;
    }

    public AppointmentCounts setActiveCount(int activeCount) {
        this.activeCount = activeCount;
        return this;
    }

    public int getInactiveCount() {
        return inactiveCount;
    }

    public AppointmentCounts setInactiveCount(int inactiveCount) {
        this.inactiveCount = inactiveCount;
        return this;
    }

    public int getResignedCount() {
        return resignedCount;
    }

    public AppointmentCounts setResignedCount(int resignedCount) {
        this.resignedCount = resignedCount;
        return this;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public AppointmentCounts setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }
}
