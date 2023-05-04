package uk.gov.companieshouse.company_appointments.converter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentsReadConverterTest {

    private CompanyAppointmentReadConverter readConverter;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        readConverter = new CompanyAppointmentReadConverter(objectMapper);
    }

    @Test
    void testConverterMethodWorksCorrectly() throws IOException {
        String inputPath = "patchAppointmentNameStatusApiExamplePut.json";
        String patchAppointmentsRequestData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));

        Document appointmentsBson = Document.parse(patchAppointmentsRequestData);
        PatchAppointmentNameStatusApi patchAppointmentNameStatusApi = readConverter.convert(appointmentsBson);
        assertThat(patchAppointmentNameStatusApi).isNotNull();
        assertThat(patchAppointmentNameStatusApi.getCompanyName()).isEqualTo("My_Large_Company");
        assertThat(patchAppointmentNameStatusApi.getCompanyStatus()).isEqualTo("active");

    }

    @Test
    void testExceptionThrownWhenConverterCalledOnDataWithIncorrectFormat() throws IOException {
        String inputPath = "delta-appointment-data.json";
        String deltaAppointmentData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));

        Document appointmentsBson = Document.parse(deltaAppointmentData);

        Executable executable = () -> readConverter.convert(appointmentsBson);

        assertThrows(IllegalArgumentException.class, executable);
    }
}