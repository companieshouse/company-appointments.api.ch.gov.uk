package uk.gov.companieshouse.company_appointments.util;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class EmptyFieldDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext context) {
        JsonNode node = context.readTree(jsonParser);
        // Default of "" matches Jackson 2's asText() for object/array nodes, which Jackson 3's asString() rejects
        String str = node.asString("");
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }
}
