package uk.gov.companieshouse.company_appointments.officerappointments;

import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;

import static java.util.Optional.ofNullable;

public class IdentificationTypeMapper {

    private IdentificationTypeMapper() {
    }

    public static IdentificationTypeEnum mapIdentificationType(String identificationType) {
        return ofNullable(identificationType)
                .map(IdentificationTypeEnum::fromValue)
                .orElse(null);
    }
}
