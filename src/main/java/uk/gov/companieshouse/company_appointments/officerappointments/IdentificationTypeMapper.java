package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;

@Component
class IdentificationTypeMapper {

    IdentificationTypeEnum map(String identificationType) {
        return ofNullable(identificationType)
                .map(IdentificationTypeEnum::fromValue)
                .orElse(null);
    }
}
