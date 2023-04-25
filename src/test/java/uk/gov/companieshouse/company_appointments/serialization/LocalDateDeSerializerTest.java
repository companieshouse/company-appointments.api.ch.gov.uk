package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.util.DateTimeFormatter;

import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalDateDeSerializerTest {

    private static final String DATE_STRING = "2015-06-26T08:31:35.058Z";

    @Mock
    private DeserializationContext deserializationContext;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private JsonNode jsonNode;

    private LocalDateDeSerializer deserializer;

    @BeforeEach
    void setUp() throws IOException {
        deserializer = new LocalDateDeSerializer();
        when(jsonParser.readValueAsTree()).thenReturn(jsonNode);
    }

    @Test
    void testDeserializeTextValue() throws IOException {
        when(jsonNode.get(any())).thenReturn(null);
        when(jsonNode.textValue()).thenReturn(DATE_STRING);
        LocalDate localDate = deserializer.deserialize(jsonParser, deserializationContext);

        Assertions.assertEquals(DateTimeFormatter.parse(DATE_STRING), localDate);
    }

    @Test
    void testDeserializeJsonNodeTextualValue() throws IOException {
        when(jsonNode.get(any())).thenReturn(jsonNode);
        when(jsonNode.getNodeType()).thenReturn(JsonNodeType.STRING);
        when(jsonNode.textValue()).thenReturn(DATE_STRING);
        LocalDate localDate = deserializer.deserialize(jsonParser, deserializationContext);

        Assertions.assertEquals(DateTimeFormatter.parse(DATE_STRING), localDate);
    }

    @Test
    void testDeserializeJsonNodeLongValue() throws IOException {
        when(jsonNode.get(any())).thenReturn(jsonNode);
        when(jsonNode.getNodeType()).thenReturn(JsonNodeType.OBJECT);
        when(jsonNode.asLong()).thenReturn(1435308155000L);
        LocalDate localDate = deserializer.deserialize(jsonParser, deserializationContext);

        Assertions.assertEquals(DateTimeFormatter.parse(DATE_STRING), localDate);
    }
}