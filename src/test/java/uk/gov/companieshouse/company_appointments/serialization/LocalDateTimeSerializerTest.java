package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalDateTimeSerializerTest {
    private static final String DATE_STRING = "2015-06-26T08:31:35.058Z";

    @Mock
    private SerializerProvider serializerProvider;
    @Mock
    private JsonGenerator jsonGenerator;

    private LocalDateTimeSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new LocalDateTimeSerializer();
    }

    @Test
    void testSerialiseValidValue() throws IOException {
        serializer.serialize(LocalDateTime.parse(DATE_STRING, DateTimeFormatter.ISO_DATE_TIME), jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeRawValue("ISODate(\"2015-06-26T08:31:35.058Z\")");
    }

    @Test
    void testSerialiseNullValue() throws IOException {
        serializer.serialize(null, jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeNull();
    }
}
