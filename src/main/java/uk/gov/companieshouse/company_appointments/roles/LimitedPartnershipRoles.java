package uk.gov.companieshouse.company_appointments.roles;

import java.util.stream.Stream;

public enum LimitedPartnershipRoles {
    GENERAL_PARTNER("general-partner-in-a-limited-partnership"),
    CORPORATE_GENERAL_PARTNER("corporate-general-partner-in-a-limited-partnership"),
    LIMITED_PARTNER("limited-partner-in-a-limited-partnership"),
    CORPORATE_LIMITED_PARTNER("corporate-limited-partner-in-a-limited-partnership");

    private final String role;

    LimitedPartnershipRoles(String role){
        this.role = role;
    }

    public static Stream<LimitedPartnershipRoles> stream() {
        return Stream.of(LimitedPartnershipRoles.values());
    }

    public String getRole(){
        return role;
    }
}
