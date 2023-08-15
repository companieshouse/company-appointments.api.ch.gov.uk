package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
class LocalDateMapper {

    LocalDate map(Instant dateTime) {
        return ofNullable(dateTime)
                .map(dt -> LocalDate.ofInstant(dt, UTC))
                .orElse(null);
    }
}
