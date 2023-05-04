package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.function.Executable;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentsFullRecordReadConverterTest {

    private CompanyAppointmentFullRecordReadConverter readConverter;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        readConverter = new CompanyAppointmentFullRecordReadConverter(objectMapper);
    }

    @Test
    void testConverterMethodWorksCorrectly() throws IOException {
        String inputPath = "fullRecordAppointmentsExamplePut.json";
        String fullRecordPutRequestData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));

        Document appointmentsBson = Document.parse(fullRecordPutRequestData);
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = readConverter.convert(appointmentsBson);
        assertThat(fullRecordCompanyOfficerApi).isNotNull();
        assertThat(fullRecordCompanyOfficerApi.getInternalData()).isNotNull();
        assertThat(fullRecordCompanyOfficerApi.getExternalData().getAppointmentId()).isEqualTo("7IjxamNGLlqtIingmTZJJ42Hw9Q");
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
