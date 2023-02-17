package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;

@Component
public class IdentificationMapper {

    private final IdentificationTypeMapper mapper;

    public IdentificationMapper(IdentificationTypeMapper mapper) {
        this.mapper = mapper;
    }

    protected CorporateIdent map(IdentificationData identificationData) {
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
