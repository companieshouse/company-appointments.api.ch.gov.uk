package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;

@Component
class NameMapper {

    String map(DeltaOfficerData data) {
        return ofNullable(data.getCompanyName())
                .orElse(buildOfficerName(data));
    }

    NameElements mapNameElements(DeltaOfficerData data) {
        NameElements nameElements = new NameElements()
                .forename(data.getForename())
                .title(data.getTitle())
                .otherForenames(data.getOtherForenames())
                .surname(data.getSurname())
                .honours(data.getHonours());
        if (nameElements.equals(new NameElements())) {
            nameElements = null;
        }
        return nameElements;
    }

    private static String buildOfficerName(DeltaOfficerData data) {
        return Stream.of(data.getForename(), data.getOtherForenames(), data.getSurname())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }
}
