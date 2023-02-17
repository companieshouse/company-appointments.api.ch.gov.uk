package uk.gov.companieshouse.company_appointments.officerappointments;

import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.company_appointments.officerappointments.IdentificationTypeMapper.mapIdentificationType;

public class IdentificationMapper {

    private IdentificationMapper() {
    }

    protected static CorporateIdent mapIdentification(IdentificationData identificationData) {
        return ofNullable(identificationData)
                .map(identification -> new CorporateIdent()
                        .identificationType(mapIdentificationType(identification.getIdentificationType()))
                        .legalAuthority(identification.getLegalAuthority())
                        .legalForm(identification.getLegalForm())
                        .placeRegistered(identification.getPlaceRegistered())
                        .registrationNumber(identification.getRegistrationNumber()))
                .orElse(null);
    }
}
