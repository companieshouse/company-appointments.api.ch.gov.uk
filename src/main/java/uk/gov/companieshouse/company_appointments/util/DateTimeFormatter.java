package uk.gov.companieshouse.company_appointments.util;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeFormatter {

    static final String STRING_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    static final Pattern PATTERN = Pattern.compile(STRING_PATTERN);

    static java.time.format.DateTimeFormatter writeDateTimeFormatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static java.time.format.DateTimeFormatter readDateTimeFormatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate parse(String dateString) {
        Matcher matcher = PATTERN.matcher(dateString);
        matcher.find();
        return LocalDate.parse(matcher.group(), readDateTimeFormatter);
    }

    public static String formattedDate(LocalDate localDate) {
        return localDate.atStartOfDay().format(writeDateTimeFormatter);
    }

}
