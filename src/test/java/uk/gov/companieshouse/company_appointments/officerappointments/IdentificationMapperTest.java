package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;

@ExtendWith(MockitoExtension.class)
class IdentificationMapperTest {

    @InjectMocks
    private IdentificationMapper mapper;
    @Mock
    private IdentificationTypeMapper identificationTypeMapper;

    @Test
    void mapIdentification() {
        // given
        when(identificationTypeMapper.map(anyString())).thenReturn(IdentificationTypeEnum.UK_LIMITED_COMPANY);
        DeltaIdentification identificationData = new DeltaIdentification()
                .setIdentificationType("uk-limited-company")
                .setLegalAuthority("legal authority")
                .setLegalForm("legal form")
                .setPlaceRegistered("place registered")
                .setRegistrationNumber("registration number")
                .setRegisterLocation("register location");

        CorporateIdent expected = new CorporateIdent()
                .identificationType(IdentificationTypeEnum.UK_LIMITED_COMPANY)
                .legalAuthority("legal authority")
                .legalForm("legal form")
                .placeRegistered("place registered")
                .registrationNumber("registration number")
                .registerLocation("register location");

        // when
        CorporateIdent actual = mapper.map(identificationData);

        // then
        assertEquals(expected, actual);
        verify(identificationTypeMapper).map("uk-limited-company");
    }

    @Test
    void mapIdentificationEmpty() {
        // given
        DeltaIdentification identificationData = new DeltaIdentification();

        CorporateIdent expected = new CorporateIdent();

        // when
        CorporateIdent actual = mapper.map(identificationData);

        // then
        assertEquals(expected, actual);
        verify(identificationTypeMapper).map(null);
    }

    @Test
    void mapIdentificationNull() {
        // given
        // when
        CorporateIdent actual = mapper.map(null);

        // then
        assertNull(actual);
        verifyNoInteractions(identificationTypeMapper);
    }
}