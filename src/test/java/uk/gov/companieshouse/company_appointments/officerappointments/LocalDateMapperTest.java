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

    @Test
    void mapNullValue() {
        // given
        // when
        LocalDate actual = mapper.map(null);

        // then
        assertNull(actual);
    }

    @Test
    void mapInstant() {
        // given
        Instant appointedOn = LocalDateTime.of(1990, 1, 20, 0, 0).toInstant(ZoneOffset.UTC);

        LocalDate expected = LocalDate.of(1990, 1, 20);

        // when
        LocalDate actual = mapper.map(appointedOn);

        // then
        assertEquals(expected, actual);
    }
}