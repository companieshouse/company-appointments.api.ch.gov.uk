package uk.gov.companieshouse.company_appointments.officerappointments;

import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;

import static java.util.Optional.ofNullable;

public class OfficerRoleMapper {

    private static final String CORPORATE = "corporate";

    private OfficerRoleMapper() {
    }

    protected static OfficerRoleEnum mapOfficerRole(String officerRole) {
        return ofNullable(officerRole)
                .map(OfficerRoleEnum::fromValue)
                .orElse(null);
    }

    protected static boolean mapIsCorporateOfficer(String officerRole) {
        return ofNullable(officerRole)
                .map(role -> role.startsWith(CORPORATE))
                .orElse(false);
    }
}
