package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentsWriteConverterTest {

    private static final String COMPANY_NAME = "My_Large_Company";

    private CompanyAppointmentWriteConverter converter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.registerModule(new JavaTimeModule());
        converter = new CompanyAppointmentWriteConverter(objectMapper1);
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
        assertThat(dbObject.toJson()).contains(COMPANY_NAME);
    }

    @Test
    void testExceptionThrownWhenConverterCalledOnNullData() {
        assertThrows(RuntimeException.class, () -> converter.convert(null));
    }
}
