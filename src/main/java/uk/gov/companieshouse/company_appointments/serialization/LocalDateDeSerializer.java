package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import uk.gov.companieshouse.company_appointments.exception.DeserializationException;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final Set<String> DATES_AS_INT_ARRAY = Set.of("appointed_on", "updated_at");

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
            DeserializationContext deserializationContext) {
        try {
            JsonNode jsonNode = jsonParser.readValueAsTree();
            if (jsonNode.isArray() && DATES_AS_INT_ARRAY.contains(jsonParser.currentName())) {
                int year = jsonNode.get(0).asInt();
                int month = jsonNode.get(1).asInt();
                int day = jsonNode.get(2).asInt();
                return LocalDate.of(year, month, day);
            } else if (jsonNode.isTextual()) {
                return LocalDate.parse(jsonNode.textValue(), dateTimeFormatter);
            } else {
                return LocalDate.ofInstant(Instant.ofEpochMilli(jsonNode.get("$numberLong").asLong()),
                                ZoneId.systemDefault());
            }
        } catch (IOException | DateTimeParseException ex) {
            throw new DeserializationException("Failed while deserializing "
                    + "date value for json node.", ex);
        }
    }
}
