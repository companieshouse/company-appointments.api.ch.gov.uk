package uk.gov.companieshouse.company_appointments.officerappointments;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static uk.gov.companieshouse.company_appointments.interceptor.AuthenticationHelperImpl.INTERNAL_APP_PRIVILEGE;

@Component
public class ItemsPerPageService {

    private static final int DEFAULT_ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE_EXTERNAL = 50;

    private final int maxItemsPerPageInternal;

    public ItemsPerPageService(@Value("${items-per-page-max-internal}") final int maxItemsPerPageInternal) {
        this.maxItemsPerPageInternal = maxItemsPerPageInternal;
    }

    public int getItemsPerPage(Integer itemsPerPage, String authPrivileges) {
        if (itemsPerPage == null || itemsPerPage == 0) {
            itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        } else {
            int maxItemsPerPage = hasInternalAppPrivileges(authPrivileges) ?
                    maxItemsPerPageInternal : MAX_ITEMS_PER_PAGE_EXTERNAL;
            itemsPerPage = Math.min(Math.abs(itemsPerPage), maxItemsPerPage);
        }
        return itemsPerPage;
    }

    private boolean hasInternalAppPrivileges(String authPrivileges) {
        return Optional.ofNullable(authPrivileges)
                .map(rawAuthPrivileges -> rawAuthPrivileges.split(","))
                .map(privileges -> ArrayUtils.contains(privileges, INTERNAL_APP_PRIVILEGE))
                .orElse(false);
    }
}
