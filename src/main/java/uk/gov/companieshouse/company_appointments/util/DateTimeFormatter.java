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

    /**
     * Parse date string to LocalDate.
     * @param dateString date as string.
     * @return parsed date.
     */
    public static LocalDate parse(String dateString) {
        Matcher matcher = PATTERN.matcher(dateString);
        matcher.find();
        return LocalDate.parse(matcher.group(), readDateTimeFormatter);
    }

    /**
     * Formats date to midnight and string
     * with pattern matching above.
     * @param localDate date to format.
     * @return formatted date as string.
     */
    public static String format(LocalDate localDate) {
        return localDate.atStartOfDay().format(writeDateTimeFormatter);
    }

}
