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
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;

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
        IdentificationData identificationData = IdentificationData.builder()
                .withIdentificationType("uk-limited-company")
                .withLegalAuthority("legal authority")
                .withLegalForm("legal form")
                .withPlaceRegistered("place registered")
                .withRegistrationNumber("registration number")
                .build();

        CorporateIdent expected = new CorporateIdent()
                .identificationType(IdentificationTypeEnum.UK_LIMITED_COMPANY)
                .legalAuthority("legal authority")
                .legalForm("legal form")
                .placeRegistered("place registered")
                .registrationNumber("registration number");

        // when
        CorporateIdent actual = mapper.map(identificationData);

        // then
        assertEquals(expected, actual);
        verify(identificationTypeMapper).map("uk-limited-company");
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