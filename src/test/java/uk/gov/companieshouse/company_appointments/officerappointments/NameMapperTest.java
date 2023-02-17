package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

class NameMapperTest {

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
        String actual = NameMapper.mapName(data);

        // then
        assertEquals("Dr John Tester Smith", actual);
    }

    @Test
    void mapNameNulls() {
        // given
        OfficerData data = OfficerData.builder()
                .withForename("John")
                .withSurname("Smith")
                .build();

        // when
        String actual = NameMapper.mapName(data);

        // then
        assertEquals("John Smith", actual);
    }

    @Test
    void mapNameAllNull() {
        // given
        OfficerData data = OfficerData.builder().build();

        // when
        String actual = NameMapper.mapName(data);

        // then
        assertEquals("", actual);
    }
}