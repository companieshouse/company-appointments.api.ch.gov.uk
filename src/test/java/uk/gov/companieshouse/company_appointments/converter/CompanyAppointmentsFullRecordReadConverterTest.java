package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.config.TestConfig;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateDeSerializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class CompanyAppointmentsFullRecordReadConverterTest {


    String fullRecordPutRequestData;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private LocalDateDeSerializer localDateDeSerializer;

    private static final LocalDate LOCAL_DATE = LocalDate.now();

    private CompanyAppointmentFullRecordReadConverter readConverter;

    @BeforeEach
    void setup() throws IOException {
        readConverter = new CompanyAppointmentFullRecordReadConverter(objectMapper);
        String inputPath = "fullRecordAppointmentsExamplePut.json";
        fullRecordPutRequestData =
                FileCopyUtils.copyToString(new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResourceAsStream(inputPath))));
    }

    @Test
    void convert() throws IOException {
        when(localDateDeSerializer.deserialize(any(), any())).thenReturn(LOCAL_DATE);

        Document appointmentsBson = Document.parse(fullRecordPutRequestData);
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = readConverter.convert(appointmentsBson);
        assertThat(fullRecordCompanyOfficerApi).isNotNull();
        assertThat(fullRecordCompanyOfficerApi.getInternalData()).isNotNull();
        assertThat(fullRecordCompanyOfficerApi.getExternalData().getAppointmentId()).isEqualTo("7IjxamNGLlqtIingmTZJJ42Hw9Q");

    }
}
