package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LocalDateMapperTest {

    @Test
    void mapAppointedBefore() {
        // given
        String appointedBefore = "1990-01-20";

        LocalDate expected = LocalDate.of(1990, 1, 20);

        // when
        LocalDate actual = LocalDateMapper.mapLocalDate(appointedBefore);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapAppointedBeforeNull() {
        // given
        // when
        LocalDate actual = LocalDateMapper.mapLocalDate((String) null);

        // then
        assertNull(actual);
    }

    @Test
    void mapAppointedOn() {
        // given
        LocalDateTime appointedOn = LocalDateTime.of(1990, 1, 20, 0, 0);

        LocalDate expected = LocalDate.of(1990, 1, 20);

        // when
        LocalDate actual = LocalDateMapper.mapLocalDate(appointedOn);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapAppointedOnNull() {
        // given
        // when
        LocalDate actual = LocalDateMapper.mapLocalDate((LocalDateTime) null);

        // then
        assertNull(actual);
    }
}