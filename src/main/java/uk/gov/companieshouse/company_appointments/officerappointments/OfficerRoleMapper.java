package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;

@Component
public class OfficerRoleMapper {

    private static final String CORPORATE = "corporate";

    protected OfficerRoleEnum mapOfficerRole(String officerRole) {
        return ofNullable(officerRole)
                .map(OfficerRoleEnum::fromValue)
                .orElse(null);
    }

    protected boolean mapIsCorporateOfficer(String officerRole) {
        return ofNullable(officerRole)
                .map(role -> role.startsWith(CORPORATE))
                .orElse(false);
    }
}
