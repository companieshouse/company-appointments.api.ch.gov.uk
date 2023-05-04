package uk.gov.companieshouse.company_appointments.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalDateSerializerTest {

    private static final String DATE_STRING = "2015-06-26";

    @Mock
    private SerializerProvider serializerProvider;
    @Mock
    private JsonGenerator jsonGenerator;

    private LocalDateSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new LocalDateSerializer();
    }

    @Test
    void testSerialiseValidValue() throws IOException {
        serializer.serialize(LocalDate.parse(DATE_STRING), jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeRawValue("ISODate(\"2015-06-26T00:00:00Z\")");
    }

    @Test
    void testSerialiseNullValue() throws IOException {
        serializer.serialize(null, jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeNull();
    }

}