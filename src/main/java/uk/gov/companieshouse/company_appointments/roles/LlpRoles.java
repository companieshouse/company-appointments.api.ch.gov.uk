package uk.gov.companieshouse.company_appointments.roles;

import java.util.stream.Stream;

public enum LlpRoles {
    LLP_MEMBER("llp-member"),
    CORPORATE_LLP_MEMBER("corporate-llp-member"),
    LLP_DESIGNATED_MEMBER("llp-designated-member"),
    CORPORATE_LLP_DESIGNATED_MEMBER("corporate-llp-designated-member");

    private final String role;

    LlpRoles(String role){
        this.role = role;
    }

    public static Stream<LlpRoles> stream() {
        return Stream.of(LlpRoles.values());
    }

    public String getRole(){
        return role;
    }
}
