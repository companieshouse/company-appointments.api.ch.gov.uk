package uk.gov.companieshouse.company_appointments.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.exception.DeserializationException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
class LocalDateDeSerializerTest {

    private static final String DATE_STRING = "2015-06-26";

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

        assertEquals(LocalDate.parse(DATE_STRING), localDate);
    }

    @Test
    void testDeserializeJsonNodeTextualValue() throws IOException {
        when(jsonNode.get(any())).thenReturn(jsonNode);
        when(jsonNode.getNodeType()).thenReturn(JsonNodeType.STRING);
        when(jsonNode.textValue()).thenReturn(DATE_STRING);
        LocalDate localDate = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(LocalDate.parse(DATE_STRING), localDate);
    }

    @Test
    void testDeserializeJsonNodeLongValue() throws IOException, ParseException {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse(DATE_STRING);
        long dateInLong = date.getTime();

        when(jsonNode.get(any())).thenReturn(jsonNode);
        when(jsonNode.getNodeType()).thenReturn(JsonNodeType.OBJECT);

        when(jsonNode.asLong()).thenReturn(1435273200000L);
        LocalDate localDate = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(LocalDate.parse(DATE_STRING), localDate);
    }

    @Test
    void testDeserializeWithException() {
        when(jsonNode.get(any())).thenThrow(DateTimeParseException.class);

        Executable executable = () -> deserializer.deserialize(jsonParser, deserializationContext);

        Exception exception = assertThrows(DeserializationException.class, executable);

        assertEquals("Failed while deserializing "
                + "date value for json node.", exception.getMessage());
    }
}