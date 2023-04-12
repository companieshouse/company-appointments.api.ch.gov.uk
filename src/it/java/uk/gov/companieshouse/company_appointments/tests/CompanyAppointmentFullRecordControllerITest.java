package uk.gov.companieshouse.company_appointments.tests;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");
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
    void testReturnFullRecordGetMapsFieldNamesCorrectly() throws Exception{
        mockMvc.perform(get("/company/{company_number}/appointments/{appointment_id}/full_record", COMPANY_NUMBER, APPOINTMENT_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "sensitive-data"))
                .andExpect(jsonPath("$.service_address.address_line_1", notNullValue()))
                .andExpect(jsonPath("$.service_address.address_line_2", notNullValue()))
                .andExpect(jsonPath("$.service_address.postal_code", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.address_line_1", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.address_line_2", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.care_of", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.po_box", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.postal_code", notNullValue()))
                .andExpect(jsonPath("$.identification.identification_type", notNullValue()))
                .andExpect(jsonPath("$.identification.legal_authority", notNullValue()))
                .andExpect(jsonPath("$.identification.legal_form", notNullValue()))
                .andExpect(jsonPath("$.identification.place_registered", notNullValue()))
                .andExpect(jsonPath("$.identification.registration_number", notNullValue()))
                .andExpect(jsonPath("$.contact_details.contact_name", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.address_line_1", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.address_line_2", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.care_of", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.po_box", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.postal_code", notNullValue()))
                .andExpect(status().isOk());
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
