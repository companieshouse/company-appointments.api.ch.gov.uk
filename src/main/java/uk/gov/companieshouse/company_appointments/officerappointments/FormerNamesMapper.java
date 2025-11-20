package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;

@Component
class FormerNamesMapper {

    List<FormerNames> map(List<DeltaFormerNames> formerNames) {
        return ofNullable(formerNames)
                .map(formerNamesData -> formerNamesData.stream()
                        .map(names -> new FormerNames()
                                .forenames(names.getForenames())
                                .surname(names.getSurname()))
                        .toList())
                .orElse(null);
    }
}
