package uk.gov.companieshouse.company_appointments.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DateTimeProcessorTest {

    private static final String INSTANT_AS_STRING = "2024-09-04T12:00:16.119110Z";

    private final DateTimeProcessor dateTimeProcessor = new DateTimeProcessor();

    @Test
    void testFormatPublishedAt() {
        // given
        Instant now = Instant.parse(INSTANT_AS_STRING);
        final String expected = "2024-09-04T12:00:16";

        // when
        final String actual = dateTimeProcessor.formatPublishedAt(now);

        // then
        assertEquals(expected, actual);
    }
}