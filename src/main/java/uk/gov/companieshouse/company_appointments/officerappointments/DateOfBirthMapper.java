package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;

@Component
class DateOfBirthMapper {

    private final OfficerRoleMapper officerRoleMapper;

    DateOfBirthMapper(OfficerRoleMapper officerRoleMapper) {
        this.officerRoleMapper = officerRoleMapper;
    }

    DateOfBirth map(Instant dateOfBirth, String officerRole) {
        if (SecretarialRoles.stream().anyMatch(roles -> roles.getRole().equals(officerRole))
                || officerRoleMapper.mapIsCorporateOfficer(officerRole)) {
            return null;
        } else {
            return ofNullable(dateOfBirth)
                    .map(dob -> ZonedDateTime.ofInstant(dob, ZoneOffset.UTC))
                    .map(zdt -> new DateOfBirth()
                            .month(zdt.getMonthValue())
                            .year(zdt.getYear()))
                    .orElse(null);
        }
    }
}
