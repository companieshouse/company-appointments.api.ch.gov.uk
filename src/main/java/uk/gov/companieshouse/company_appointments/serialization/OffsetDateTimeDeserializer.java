package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;


public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        try {
            return OffsetDateTime.parse(jsonParser.getText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception exception) {
            LOGGER.error("OffsetDateTime Deserialization failed.", exception);
            throw new RuntimeException("Failed while deserializing "
                    + "date value for json node.", exception);
        }
    }
}
