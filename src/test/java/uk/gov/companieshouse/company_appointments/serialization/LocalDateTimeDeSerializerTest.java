package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.exception.DeserializationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalDateTimeDeSerializerTest {

    private static final String DATE_STRING = "2015-06-26T08:31:35.058Z";

    @Mock
    private DeserializationContext deserializationContext;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private JsonNode jsonNode;

    private LocalDateTimeDeSerializer deserializer;

    @BeforeEach
    void setUp() throws IOException {
        deserializer = new LocalDateTimeDeSerializer();
        when(jsonParser.readValueAsTree()).thenReturn(jsonNode);
    }

    @Test
    void testDeserializeTextValue() {
        when(jsonNode.get(any())).thenReturn(jsonNode);
        when(jsonNode.textValue()).thenReturn(DATE_STRING);
        LocalDateTime localDateTime = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(LocalDateTime.parse(DATE_STRING, DateTimeFormatter.ISO_DATE_TIME), localDateTime);
    }

    @Test
    void testDeserializeWithException() {
        when(jsonNode.get(any())).thenReturn(jsonNode);
        when(jsonNode.textValue()).thenThrow(DateTimeParseException.class);

        Executable executable = () -> deserializer.deserialize(jsonParser, deserializationContext);

        Exception exception = assertThrows(DeserializationException.class, executable);

        assertEquals("Failed while deserializing "
                + "date value for json node.", exception.getMessage());
    }
}
