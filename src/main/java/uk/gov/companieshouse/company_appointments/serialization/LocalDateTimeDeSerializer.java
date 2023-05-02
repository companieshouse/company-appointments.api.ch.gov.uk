package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.company_appointments.exception.DeserializationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeSerializer extends JsonDeserializer<LocalDateTime> {
    public static final String APPLICATION_NAME_SPACE = "company-appointments.api.ch.gov.uk";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            JsonNode jsonNode = jsonParser.readValueAsTree();
            return jsonNode.get("$date") == null ? LocalDateTime.parse(jsonNode.textValue(),
                    dateTimeFormatter) :
                    LocalDateTime.parse(jsonNode.get("$date").textValue(), dateTimeFormatter);

        } catch (IOException | DateTimeParseException exception) {
            LOGGER.error("LocalDateTime Deserialization failed.", exception);
            throw new DeserializationException("Failed while deserializing "
                    + "date value for json node.", exception);
        }
    }
}