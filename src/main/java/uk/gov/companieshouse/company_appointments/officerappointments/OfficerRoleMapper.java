package uk.gov.companieshouse.company_appointments.officerappointments;

import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;

import static java.util.Optional.ofNullable;

public class OfficerRoleMapper {

    private OfficerRoleMapper() {
    }

    public static OfficerRoleEnum mapOfficerRole(String officerRole) {
        return ofNullable(officerRole)
                .map(OfficerRoleEnum::fromValue)
                .orElse(null);
    }
}
