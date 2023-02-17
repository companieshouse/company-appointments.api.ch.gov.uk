package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum.UK_LIMITED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;

class IdentificationTypeMapperTest {

    private IdentificationTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IdentificationTypeMapper();
    }

    @Test
    void mapIdentificationType() {
        // given
        String identificationType = "uk-limited";

        // when
        IdentificationTypeEnum actual = mapper.map(identificationType);

        // then
        assertEquals(UK_LIMITED, actual);
    }

    @Test
    void mapIdentificationTypeNull() {
        // given
        // when
        IdentificationTypeEnum actual = mapper.map(null);

        // then
        assertNull(actual);
    }

    @Test
    void mapIdentificationTypeIncorrect() {
        // given
        String identificationType = "incorrect-id-type";

        // when
        Executable executable = () -> mapper.map(identificationType);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Unexpected value '%s'", identificationType), exception.getMessage());
    }
}