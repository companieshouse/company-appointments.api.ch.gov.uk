package uk.gov.companieshouse.company_appointments.roles;

import java.util.stream.Stream;

public enum SecretarialRoles{
    SECRETARY("secretary"),
    CORPORATE_SECRETARY("corporate-secretary"),
    NOMINEE_SECRETARY("nominee-secretary"),
    CORPORATE_NOMINEE_SECRETARY("corporate-nominee-secretary");

    private final String role;

    SecretarialRoles(String role){
        this.role = role;
    }

    public static Stream<SecretarialRoles> stream() {
        return Stream.of(SecretarialRoles.values());
    }

    public String getRole(){
        return role;
    }
}
