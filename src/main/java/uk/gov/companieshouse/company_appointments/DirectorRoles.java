package uk.gov.companieshouse.company_appointments;

import java.util.stream.Stream;

public enum DirectorRoles {
    DIRECTOR("director"),
    CORPORATE_DIRECTOR("corporate-director"),
    NOMINEE_DIRECTOR("nominee-director"),
    CORPORATE_NOMINEE_DIRECTOR("corporate-nominee-director");

    private String role;

    DirectorRoles(String role){
        this.role = role;
    }

    public static Stream<DirectorRoles> stream() {
        return Stream.of(DirectorRoles.values());
    }

    public String getRole(){
        return role;
    }
}
