package uk.gov.companieshouse.company_appointments.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DateTimeProcessor {

    private static final DateTimeFormatter publishedAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String formatPublishedAt(Instant now) {
        return publishedAtFormatter.format(now.atZone(ZoneOffset.UTC));
    }
}
