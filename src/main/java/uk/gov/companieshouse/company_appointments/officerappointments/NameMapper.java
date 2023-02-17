package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

public class NameMapper {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";

    private NameMapper() {
    }

    protected static String mapName(OfficerData data) {
        return ofNullable(data.getCompanyName())
                .orElse(buildOfficerName(data));
    }

    protected static String buildOfficerName(OfficerData data) {
        String result = Stream.of(data.getForename(), data.getOtherForenames(), data.getSurname())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        if (data.getTitle() != null && !data.getTitle().matches(TITLE_REGEX)) {
            result = String.format("%s %s", data.getTitle(), result);
        }
        return result;
    }
}
