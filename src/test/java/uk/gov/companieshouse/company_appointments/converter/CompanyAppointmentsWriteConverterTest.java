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
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentsWriteConverterTest {

    private static final String COMPANY_NAME = "company_name";
    private CompanyAppointmentWriteConverter converter;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        converter = new CompanyAppointmentWriteConverter(objectMapper);
    }

    @Test
    void canSuccessfullyConvertDocument() throws IOException {
        String inputPath = "patchAppointmentNameStatusApiExamplePut.json";
        String patchAppointmentsRequestData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));
        PatchAppointmentNameStatusApi patchAppointmentNameStatusApi = objectMapper
                .readValue(patchAppointmentsRequestData, PatchAppointmentNameStatusApi.class);

        BasicDBObject dbObject = converter.convert(patchAppointmentNameStatusApi);
        assertNotNull(dbObject);
        assertTrue(dbObject.containsField(COMPANY_NAME));
    }

    @Test
    void testExceptionThrownWhenConverterCalledOnNullData() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});

        Executable executable = () -> converter.convert(any());

        assertThrows(IllegalArgumentException.class, executable);
    }
}
