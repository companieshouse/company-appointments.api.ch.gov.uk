package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.stereotype.Component;

@Component
class AppointmentsCountsService {

    private final OfficerAppointmentsRepository repository;

    AppointmentsCountsService(OfficerAppointmentsRepository repository) {
        this.repository = repository;
    }

    AppointmentsCounts getAppointmentsCounts(String officerId, boolean isFilterEnabled, Integer totalCount) {
        AppointmentsCounts appointmentsCounts;
        if (isFilterEnabled) {
            appointmentsCounts = new AppointmentsCounts()
                    .activeCount(totalCount)
                    .inactiveCount(0)
                    .resignedCount(0);
        } else {
            appointmentsCounts = repository.countOfficerAppointments(officerId);
            appointmentsCounts.activeCount(totalCount - appointmentsCounts.getInactiveCount() - appointmentsCounts.getResignedCount());
        }
        return appointmentsCounts;
    }
}
