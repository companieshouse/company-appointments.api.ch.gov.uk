package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.company_appointments.exception.DeserializationException;
import uk.gov.companieshouse.company_appointments.util.DateTimeFormatter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) {
        try {
        JsonNode jsonNode = jsonParser.readValueAsTree();
            JsonNode dateJsonNode = jsonNode.get("$date");
            if (dateJsonNode == null) {
                return DateTimeFormatter.parse(jsonNode.textValue());
            } else if (dateJsonNode.isTextual()) {
                return DateTimeFormatter.parse(dateJsonNode.textValue());
            } else {
                long longDate = dateJsonNode.get("$numberLong").asLong();
                String dateString = Instant.ofEpochMilli(new Date(longDate).getTime()).toString();
                return DateTimeFormatter.parse(dateString);
            }
        } catch (IOException | DateTimeParseException ex) {
            throw new DeserializationException("Failed while deserializing "
                    + "date value for json node.", ex);
        }
    }
}
