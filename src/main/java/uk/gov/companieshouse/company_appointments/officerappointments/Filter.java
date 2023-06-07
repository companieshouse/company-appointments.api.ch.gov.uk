package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

public class Filter {

    private final boolean filterEnabled;
    private final ToIntFunction<AppointmentCounts> activeCountFormula;
    private final List<String> filterStatuses;

    public Filter(boolean filterEnabled, ToIntFunction<AppointmentCounts> activeCountFormula, List<String> filterStatuses) {
        this.filterEnabled = filterEnabled;
        this.activeCountFormula = activeCountFormula;
        this.filterStatuses = filterStatuses;
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    public ToIntFunction<AppointmentCounts> getActiveCountFormula() {
        return activeCountFormula;
    }

    public List<String> getFilterStatuses() {
        return filterStatuses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filter filter = (Filter) o;
        return filterEnabled == filter.filterEnabled && Objects.equals(activeCountFormula,
                filter.activeCountFormula) && Objects.equals(filterStatuses, filter.filterStatuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterEnabled, activeCountFormula, filterStatuses);
    }

    @Override
    public String toString() {
        return "Filter{" +
                "filterEnabled=" + filterEnabled +
                ", activeCountFormula=" + activeCountFormula +
                ", filterStatuses=" + filterStatuses +
                '}';
    }
}
