package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;

public class FormerNamesMapper {

    private FormerNamesMapper() {
    }

    protected static List<FormerNames> mapFormerNames(List<FormerNamesData> formerNames) {
        return ofNullable(formerNames)
                .map(formerNamesData -> formerNamesData.stream()
                        .map(names -> new FormerNames()
                                .forenames(names.getForenames())
                                .surname(names.getSurname()))
                        .collect(Collectors.toList()))
                .orElse(null);
    }
}
