package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class LocalDateMapper {

    protected LocalDate map(String dateString) {
        return ofNullable(dateString)
                .map(appointed -> LocalDate.parse(appointed, DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK)))
                .orElse(null);
    }

    protected LocalDate map(LocalDateTime dateTime) {
        return ofNullable(dateTime)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }
}
