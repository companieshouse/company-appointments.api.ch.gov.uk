package uk.gov.companieshouse.company_appointments.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentsFullRecordWriteConverterTest {

    private static final String EXTERNAL_DATA = "external_data";
    private static final String APPOINTMENT_ID = "appointment_id";
    private CompanyAppointmentFullRecordWriteConverter converter;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        converter = new CompanyAppointmentFullRecordWriteConverter(objectMapper);
    }

    @Test
    void canSuccessfullyConvertDocument() throws IOException {
        String inputPath = "fullRecordAppointmentsExamplePut.json";
        String fullRecordPutRequestData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(fullRecordPutRequestData, FullRecordCompanyOfficerApi.class);

        BasicDBObject dbObject = converter.convert(fullRecordCompanyOfficerApi);
        assertNotNull(dbObject);
        BasicDBObject externalData = (BasicDBObject) dbObject.get(EXTERNAL_DATA);
        assertNotNull(externalData);
        assertTrue(externalData.containsField(APPOINTMENT_ID));
    }

    @Test
    void testExceptionThrownWhenConverterCalledOnDataWithIncorrectFormat() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});

        Executable executable = () -> converter.convert(any());

        assertThrows(IllegalArgumentException.class, executable);
    }
}
