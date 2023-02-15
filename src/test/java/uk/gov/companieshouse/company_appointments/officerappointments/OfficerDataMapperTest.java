package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.company_appointments.SecretarialRoles;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

class OfficerDataMapperTest {

    @Test
    void mapDateOfBirth() {
        // given
        OfficerData data = OfficerData.builder()
                .withDateOfBirth(LocalDateTime.of(2000, 2, 5, 0, 0))
                .build();

        DateOfBirth expected = new DateOfBirth()
                .month(2)
                .year(2000);

        // when
        DateOfBirth actual = OfficerDataMapper.mapDateOfBirth(data);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapDateOfBirthSecretary() {
        // given
        OfficerData data = OfficerData.builder()
                .withOfficerRole(SecretarialRoles.SECRETARY.getRole())
                .build();

        // when
        DateOfBirth actual = OfficerDataMapper.mapDateOfBirth(data);

        // then
        assertNull(actual);
    }

    @Test
    void mapDateOfBirthNull() {
        // given
        OfficerData data = OfficerData.builder().build();

        // when
        DateOfBirth actual = OfficerDataMapper.mapDateOfBirth(data);

        // then
        assertNull(actual);
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
        String actual = OfficerDataMapper.mapName(data);

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
        String actual = OfficerDataMapper.mapName(data);

        // then
        assertEquals("John Smith", actual);
    }

    @Test
    void mapNameAllNull() {
        // given
        OfficerData data = OfficerData.builder().build();

        // when
        String actual = OfficerDataMapper.mapName(data);

        // then
        assertEquals("", actual);
    }

    @Test
    void mapIsCorporateOfficer() {
        // given
        OfficerData data = OfficerData.builder()
                .withOfficerRole("corporate-director")
                .build();

        // when
        boolean actual = OfficerDataMapper.mapIsCorporateOfficer(data);

        // then
        assertTrue(actual);
    }

    @Test
    void mapIsCorporateOfficerFalse() {
        // given
        OfficerData data = OfficerData.builder()
                .withOfficerRole("director")
                .build();

        // when
        boolean actual = OfficerDataMapper.mapIsCorporateOfficer(data);

        // then
        assertFalse(actual);
    }

    @Test
    void mapIsCorporateOfficerNull() {
        // given
        OfficerData data = OfficerData.builder().build();

        // when
        boolean actual = OfficerDataMapper.mapIsCorporateOfficer(data);

        // then
        assertFalse(actual);
    }
}