package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

@Component
public class NameMapper {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";

    protected String map(OfficerData data) {
        return ofNullable(data.getCompanyName())
                .orElse(buildOfficerName(data));
    }

    private String buildOfficerName(OfficerData data) {
        String result = Stream.of(data.getForename(), data.getOtherForenames(), data.getSurname())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        if (data.getTitle() != null && !data.getTitle().matches(TITLE_REGEX)) {
            result = String.format("%s %s", data.getTitle(), result);
        }
        return result;
    }
}
