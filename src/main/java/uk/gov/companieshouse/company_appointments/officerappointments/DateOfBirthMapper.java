package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;

@Component
class DateOfBirthMapper {

    private final OfficerRoleMapper officerRoleMapper;

    DateOfBirthMapper(OfficerRoleMapper officerRoleMapper) {
        this.officerRoleMapper = officerRoleMapper;
    }

    DateOfBirth map(LocalDateTime dateOfBirth, String officerRole) {
        if (SecretarialRoles.stream().anyMatch(roles -> roles.getRole().equals(officerRole))
                || officerRoleMapper.mapIsCorporateOfficer(officerRole)) {
            return null;
        } else {
            return ofNullable(dateOfBirth)
                    .map(dob -> new DateOfBirth()
                            .month(dob.getMonthValue())
                            .year(dob.getYear()))
                    .orElse(null);
        }
    }
}
