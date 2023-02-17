package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;

class IdentificationMapperTest {

    private IdentificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IdentificationMapper(new IdentificationTypeMapper());
    }

    @Test
    void mapIdentification() {
        // given
        IdentificationData identificationData = IdentificationData.builder()
                .withIdentificationType("uk-limited")
                .withLegalAuthority("legal authority")
                .withLegalForm("legal form")
                .withPlaceRegistered("place registered")
                .withRegistrationNumber("registration number")
                .build();

        CorporateIdent expected = new CorporateIdent()
                .identificationType(IdentificationTypeEnum.UK_LIMITED)
                .legalAuthority("legal authority")
                .legalForm("legal form")
                .placeRegistered("place registered")
                .registrationNumber("registration number");

        // when
        CorporateIdent actual = mapper.map(identificationData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapIdentificationEmpty() {
        // given
        IdentificationData identificationData = IdentificationData.builder().build();

        CorporateIdent expected = new CorporateIdent();

        // when
        CorporateIdent actual = mapper.map(identificationData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapIdentificationNull() {
        // given
        // when
        CorporateIdent actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}