package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.function.ToIntFunction;

class Filter {

    private final boolean filterEnabled;
    private final ToIntFunction<AppointmentCounts> activeCountFormula;
    private final List<String> filterStatuses;

    Filter(boolean filterEnabled, ToIntFunction<AppointmentCounts> activeCountFormula, List<String> filterStatuses) {
        this.filterEnabled = filterEnabled;
        this.activeCountFormula = activeCountFormula;
        this.filterStatuses = filterStatuses;
    }

    boolean isFilterEnabled() {
        return filterEnabled;
    }

    ToIntFunction<AppointmentCounts> getActiveCountFormula() {
        return activeCountFormula;
    }

    List<String> getFilterStatuses() {
        return filterStatuses;
    }
}
