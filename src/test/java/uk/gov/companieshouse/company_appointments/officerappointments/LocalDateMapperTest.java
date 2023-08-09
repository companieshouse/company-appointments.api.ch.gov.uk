package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalDateMapperTest {

    private LocalDateMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LocalDateMapper();
    }

//    @Test
//    void mapAppointedBefore() {
//        // given
//        String appointedBefore = "1990-01-20";
//
//        Instant expected = LocalDate.of(1990, 1, 20).atStartOfDay().toInstant(ZoneOffset.UTC);
//
//        // when
//        LocalDate actual = mapper.map(appointedBefore);
//
//        // then
//        assertEquals(expected, actual);
//    }

    @Test
    void mapAppointedBeforeNull() {
        // given
        // when
        LocalDate actual = mapper.map(null);

        // then
        assertNull(actual);
    }

    @Test
    void mapAppointedOn() {
        // given
        Instant appointedOn = LocalDateTime.of(1990, 1, 20, 0, 0).toInstant(ZoneOffset.UTC);

        LocalDate expected = LocalDate.of(1990, 1, 20);

        // when
        LocalDate actual = mapper.map(appointedOn);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapAppointedOnNull() {
        // given
        // when
        LocalDate actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}