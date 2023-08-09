package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;

class NameMapperTest {

    private NameMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NameMapper();
    }

    @Test
    void mapName() {
        // given
        DeltaOfficerData data = new DeltaOfficerData()
                .setTitle("Dr")
                .setForename("John")
                .setOtherForenames("Tester")
                .setSurname("Smith");

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("John Tester Smith", actual);
    }

    @Test
    void mapNameCommonTitle() {
        // given
        DeltaOfficerData data = new DeltaOfficerData()
                .setTitle("Mr")
                .setForename("John")
                .setOtherForenames("Tester")
                .setSurname("Smith");

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("John Tester Smith", actual);
    }

    @Test
    void mapNameNulls() {
        // given
        DeltaOfficerData data = new DeltaOfficerData()
                .setForename("John")
                .setSurname("Smith");

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("John Smith", actual);
    }

    @Test
    void mapNameAllNull() {
        // given
        DeltaOfficerData data = new DeltaOfficerData();

        // when
        String actual = mapper.map(data);

        // then
        assertEquals("", actual);
    }

    @Test
    void mapNameElements() {
        // given
        DeltaOfficerData data = new DeltaOfficerData()
                .setTitle("Dr")
                .setForename("John")
                .setOtherForenames("Tester")
                .setSurname("Smith")
                .setHonours("PhD");

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
        DeltaOfficerData data = new DeltaOfficerData();

        // when
        NameElements actual = mapper.mapNameElements(data);

        //then
        assertNull(actual);
    }
}