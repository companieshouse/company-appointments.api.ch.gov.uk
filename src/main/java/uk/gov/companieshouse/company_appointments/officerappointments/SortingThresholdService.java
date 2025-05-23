package uk.gov.companieshouse.company_appointments.officerappointments;

import static uk.gov.companieshouse.company_appointments.interceptor.AuthenticationHelperImpl.hasInternalAppPrivileges;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class SortingThresholdService {

    private final int internalSortingThreshold;
    private final int externalSortingThreshold;

    SortingThresholdService(@Value("${officer-appointments.sorting-threshold-internal}") final int internalSortingThreshold,
            @Value("${officer-appointments.sorting-threshold-external}") final int externalSortingThreshold) {
        this.internalSortingThreshold = internalSortingThreshold;
        this.externalSortingThreshold = externalSortingThreshold;
    }

    boolean shouldSortByActiveThenResigned(int totalResults, String authPrivileges) {
        int sortingThreshold = hasInternalAppPrivileges(authPrivileges) ? internalSortingThreshold : externalSortingThreshold;
        return sortingThreshold == -1 || totalResults <= sortingThreshold;
    }
}
