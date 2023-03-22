package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

class NameMapperTest {

    private NameMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NameMapper();
    }

    @Test
    void mapName() {
        // given
        OfficerData data = OfficerData.builder()
                .withTitle("Dr")
                .withForename("John")
                .withOtherForenames("Tester")
                .withSurname("Smith")
                .build();

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("John Tester Smith", actual);
    }

    @Test
    void mapNameCommonTitle() {
        // given
        OfficerData data = OfficerData.builder()
                .withTitle("Mr")
                .withForename("John")
                .withOtherForenames("Tester")
                .withSurname("Smith")
                .build();

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("John Tester Smith", actual);
    }

    @Test
    void mapNameNulls() {
        // given
        OfficerData data = OfficerData.builder()
                .withForename("John")
                .withSurname("Smith")
                .build();

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("John Smith", actual);
    }

    @Test
    void mapNameAllNull() {
        // given
        OfficerData data = OfficerData.builder().build();

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("", actual);
    }

    @Test
    void mapNameElements() {
        // given
        OfficerData data = OfficerData.builder()
                .withTitle("Dr")
                .withForename("John")
                .withOtherForenames("Tester")
                .withSurname("Smith")
                .withHonours("PhD")
                .build();

        NameElements expected = new NameElements()
                .forename("John")
                .title("Dr")
                .otherForenames("Tester")
                .surname("Smith")
                .honours("PhD");
        // when
        NameElements actual = mapper.mapNameElements(data);

        //then
        assertEquals(expected, actual);
    }

    @Test
    void mapEmptyNameElements() {
        // given
        OfficerData data = OfficerData.builder().build();

        // when
        NameElements actual = mapper.mapNameElements(data);

        //then
        assertNull(actual);
    }
}