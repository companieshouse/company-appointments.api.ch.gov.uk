package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;

@Component
class IdentificationMapper {

    private final IdentificationTypeMapper mapper;

    IdentificationMapper(IdentificationTypeMapper mapper) {
        this.mapper = mapper;
    }

    CorporateIdent map(DeltaIdentification identificationData) {
        return ofNullable(identificationData)
                .map(identification -> new CorporateIdent()
                        .identificationType(mapper.map(identification.getIdentificationType()))
                        .legalAuthority(identification.getLegalAuthority())
                        .legalForm(identification.getLegalForm())
                        .placeRegistered(identification.getPlaceRegistered())
                        .registrationNumber(identification.getRegistrationNumber()))
                .orElse(null);
    }
}
