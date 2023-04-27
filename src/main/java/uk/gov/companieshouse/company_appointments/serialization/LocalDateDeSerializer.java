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
import java.util.Date;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        try {
             JsonNode dateJsonNode = jsonNode.get("$date");
            if (dateJsonNode == null) {
                return DateTimeFormatter.parse(jsonNode.textValue());
            } else if (dateJsonNode.isTextual()) {
                String dateString = dateJsonNode.textValue();
                return DateTimeFormatter.parse(dateString);
            } else {
                long longDate = dateJsonNode.get("$numberLong").asLong();
                String dateString = Instant.ofEpochMilli(new Date(longDate).getTime()).toString();
                return DateTimeFormatter.parse(dateString);
            }
        } catch (Exception ex) {
            throw new DeserializationException(String.format("Failed while deserializing "
                    + "date value for json node: %s", jsonNode), ex);
        }
    }
}
