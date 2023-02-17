package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateMapper {

    private LocalDateMapper() {
    }

    protected static LocalDate mapLocalDate(String dateString) {
        return ofNullable(dateString)
                .map(appointed -> LocalDate.parse(appointed, DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK)))
                .orElse(null);
    }

    protected static LocalDate mapLocalDate(LocalDateTime dateTime) {
        return ofNullable(dateTime)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }
}
