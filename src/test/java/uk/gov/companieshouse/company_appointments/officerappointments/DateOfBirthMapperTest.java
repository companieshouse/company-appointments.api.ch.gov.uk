package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.DateOfBirth;

class DateOfBirthMapperTest {

    private DateOfBirthMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DateOfBirthMapper(new OfficerRoleMapper());
    }

    @Test
    void mapDateOfBirth() {
        // given
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 2, 5, 0, 0);

        DateOfBirth expected = new DateOfBirth()
                .month(2)
                .year(2000);

        // when
        DateOfBirth actual = mapper.map(dateOfBirth, "director");

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapDateOfBirthSecretary() {
        // given
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 2, 5, 0, 0);


        // when
        DateOfBirth actual = mapper.map(dateOfBirth, "secretary");

        // then
        assertNull(actual);
    }

    @Test
    void mapDateOfBirthNull() {
        // given

        // when
        DateOfBirth actual = mapper.map(null, "");

        // then
        assertNull(actual);
    }

    @Test
    void mapDateOfBirthCorporateOfficer() {
        // given
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 2, 5, 0, 0);

        // when
        DateOfBirth actual = mapper.map(dateOfBirth, "corporate-director");

        // then
        assertNull(actual);
    }

    @Test
    void mapDateOfBirthNullOfficerRole() {
        // given
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 2, 5, 0, 0);

        DateOfBirth expected = new DateOfBirth()
                .month(2)
                .year(2000);

        // when
        DateOfBirth actual = mapper.map(dateOfBirth, null);

        // then
        assertEquals(expected, actual);
    }
}