package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;

class Filter {

    private final boolean filterEnabled;
    private final List<String> filterStatuses;

    Filter(boolean filterEnabled, List<String> filterStatuses) {
        this.filterEnabled = filterEnabled;
        this.filterStatuses = filterStatuses;
    }

    boolean isFilterEnabled() {
        return filterEnabled;
    }

    List<String> getFilterStatuses() {
        return filterStatuses;
    }
}
