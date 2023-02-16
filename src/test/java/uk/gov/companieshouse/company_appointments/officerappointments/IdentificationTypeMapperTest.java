package uk.gov.companieshouse.company_appointments.officerappointments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum.UK_LIMITED;

class IdentificationTypeMapperTest {

    @Test
    void mapIdentificationType() {
        // given
        String identificationType = "uk-limited";

        // when
        IdentificationTypeEnum actual = IdentificationTypeMapper.mapIdentificationType(identificationType);

        // then
        assertEquals(UK_LIMITED, actual);
    }

    @Test
    void mapIdentificationTypeNull() {
        // given
        // when
        IdentificationTypeEnum actual = IdentificationTypeMapper.mapIdentificationType(null);

        // then
        assertNull(actual);
    }

    @Test
    void mapIdentificationTypeIncorrect() {
        // given
        String identificationType = "incorrect-id-type";

        // when
        Executable executable = () -> IdentificationTypeMapper.mapIdentificationType(identificationType);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Unexpected value '%s'", identificationType), exception.getMessage());
    }
}