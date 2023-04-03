package uk.gov.companieshouse.company_appointments.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompanyAppointmentFullRecordControllerITest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String CONTEXT_ID = "5234234234";
    private static final String APPOINTMENT_URL = "/company/%s/appointments/%s/full_record/delete";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private CompanyAppointmentFullRecordService companyAppointmentService;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    @DisplayName("DELETE appointment full record")
    void testReturn200OkToDeleteAppointment() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-request-id", "5234234234");
        headers.add("ERIC-Identity" , "SOME_IDENTITY");
        headers.add("ERIC-Identity-Type", "key");
        headers.add("ERIC-Authorised-Key-Privileges", "internal-app");

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(String.format(APPOINTMENT_URL, COMPANY_NUMBER, APPOINTMENT_ID),
                HttpMethod.DELETE, request, Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Return not found if appointment id incorrect")
    void testReturn404NotFoundIfAppointmentNotDeleted() throws NotFoundException, ServiceUnavailableException {

        doThrow(new NotFoundException(String.format("Appointment [%s] for company [%s] not found", "incorrect", COMPANY_NUMBER)))
                .when(companyAppointmentService).deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER, "incorrect");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-request-id", "5234234234");
        headers.add("ERIC-Identity" , "SOME_IDENTITY");
        headers.add("ERIC-Identity-Type", "key");
        headers.add("ERIC-Authorised-Key-Privileges", "internal-app");

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(String.format(APPOINTMENT_URL, COMPANY_NUMBER, "incorrect"),
                HttpMethod.DELETE, request, Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
