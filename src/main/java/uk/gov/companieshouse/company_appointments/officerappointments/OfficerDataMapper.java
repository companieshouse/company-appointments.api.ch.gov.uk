package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.company_appointments.SecretarialRoles;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

public class OfficerDataMapper {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";
    private static final String CORPORATE = "corporate";

    private OfficerDataMapper() {
    }

    public static DateOfBirth mapDateOfBirth(OfficerData data) {
        if (SecretarialRoles.stream().anyMatch(roles -> roles.getRole().equals(data.getOfficerRole()))) {
            return null;
        } else {
            return ofNullable(data.getDateOfBirth())
                    .map(dateOfBirth -> new DateOfBirth()
                            .month(dateOfBirth.getMonthValue())
                            .year(dateOfBirth.getYear()))
                    .orElse(null);
        }
    }

    public static boolean mapIsCorporateOfficer(OfficerData data) {
        return ofNullable(data.getOfficerRole())
                .map(role -> role.startsWith(CORPORATE))
                .orElse(false);
    }

    public static String mapName(OfficerData data) {
        return ofNullable(data.getCompanyName())
                .orElse(buildOfficerName(data));
    }

    private static String buildOfficerName(OfficerData data) {
        String result = Stream.of(data.getForename(), data.getOtherForenames(), data.getSurname())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        if (data.getTitle() != null && !data.getTitle().matches(TITLE_REGEX)) {
            result = String.format("%s %s", data.getTitle(), result);
        }
        return result;
    }
}
