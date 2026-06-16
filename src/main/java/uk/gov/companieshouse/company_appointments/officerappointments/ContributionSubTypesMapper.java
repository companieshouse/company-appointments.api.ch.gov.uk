package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.ContributionSubType;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContributionSubType;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
class ContributionSubTypesMapper {

    List<ContributionSubType> map(List<DeltaContributionSubType> contributionSubTypes) {
        return ofNullable(contributionSubTypes)
                .map(subTypesData -> subTypesData.stream()
                        .map(subTypes -> new ContributionSubType()
                                .subType(subTypes.getSubType()))
                        .toList())
                .orElse(null);
    }
}
