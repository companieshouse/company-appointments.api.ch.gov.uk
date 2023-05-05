package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.DeserializationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
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