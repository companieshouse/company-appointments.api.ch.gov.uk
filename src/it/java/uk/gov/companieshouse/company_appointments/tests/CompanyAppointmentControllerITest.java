package uk.gov.companieshouse.company_appointments.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyAppointmentControllerITest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResourceChangedApiService resourceChangedApiService;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.createCollection("delta_appointments");
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void testReturn200OKIfOfficerIsFound() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, "active_1")
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointed_on", is("2020-08-26")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date_of_birth.month", is(1)));
    }

    @Test
    void testReturn404IfOfficerIsNotFound() throws Exception {
        // when
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, "missing")
                        .header(ERIC_IDENTITY, "123").header(ERIC_IDENTITY_TYPE, "oauth2")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void testReturn401IfUserNotAuthenticated() throws Exception {
        // when
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER,
                        APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testReturn200OKIfAllOfficersAreFound() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers-test", COMPANY_NUMBER)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("Doe, John Forename")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(2)));
    }
    @Test
    void testReturn404IfOfficersForCompanyIsNotFound() throws Exception {
        // when
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/officers-test", "87654321")
                        .header(ERIC_IDENTITY, "123").header(ERIC_IDENTITY_TYPE, "oauth2")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testReturn200OKIfAllOfficersAreFoundWithFilter() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers-test?filter=active", COMPANY_NUMBER)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(1)));
    }

    @Test
    void testReturn200OkWithOfficersOrderedByAppointedOn() throws Exception {
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers-test?order_by=appointed_on", COMPANY_NUMBER)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("Doe, John Forename")));
    }

    @Test
    void testReturn200OkWithOfficersOrderedBySurname() throws Exception {
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers-test?order_by=surname", COMPANY_NUMBER)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("Doe, John Forename")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("NOSURNAME, Noname1 Noname2")));
    }

    @Test
    void testReturn400BadRequestWithIncorrectOrderBy() throws Exception {
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers-test?order_by=invalid", COMPANY_NUMBER)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Returns 200 ok when PATCH request handled successfully")
    void testPatchNewAppointmentCompanyNameStatus() throws Exception {
        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("active");

        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, APPOINTMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(X_REQUEST_ID, "5342342")
                    .header(ERIC_IDENTITY, "SOME_IDENTITY")
                    .header(ERIC_IDENTITY_TYPE, "key")
                    .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                    .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, String.format("/company/%s/appointments/%s", COMPANY_NUMBER, APPOINTMENT_ID)));
    }

    @Test
    @DisplayName("Patch endpoint returns 400 bad request when company name is missing")
    void testPatchNewAppointmentCompanyNameStatusMissingRequestFields() throws Exception {
        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_REQUEST_ID, "5342342")
                .header(ERIC_IDENTITY, "SOME_IDENTITY")
                .header(ERIC_IDENTITY_TYPE, "key")
                .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                .content(objectMapper.writeValueAsString(new PatchAppointmentNameStatusApi())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Patch endpoint returns 400 when invalid company status provided")
    void testPatchNewAppointmentCompanyNameStatusInvalidStatus() throws Exception {
        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("fake");

        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_REQUEST_ID, "5342342")
                .header(ERIC_IDENTITY, "SOME_IDENTITY")
                .header(ERIC_IDENTITY_TYPE, "key")
                .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Patch endpoint returns 403 forbidden when internal app privileges is missing")
    void testPatchNewAppointmentCompanyNameStatusAppPrivilegesMissing() throws Exception {
        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_REQUEST_ID, "5342342")
                .header(ERIC_IDENTITY, "SOME_IDENTITY")
                .header(ERIC_IDENTITY_TYPE, "key")
                .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "invalid"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Patch endpoint returns 404 not found when appointment does not exist")
    void testPatchNewAppointmentCompanyNameStatusAppointmentDoesNotExist() throws Exception {
        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("active");

        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, "fakeid")
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_REQUEST_ID, "5342342")
                .header(ERIC_IDENTITY, "SOME_IDENTITY")
                .header(ERIC_IDENTITY_TYPE, "key")
                .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Patch endpoint returns 503 service unavailable when resource changed endpoint unavailable")
    void testPatchNewAppointmentCompanyNameStatusApiServiceUnavailable() throws Exception {

        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(ServiceUnavailableException.class);

        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("active");

        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(X_REQUEST_ID, "5342342")
                .header(ERIC_IDENTITY, "SOME_IDENTITY")
                .header(ERIC_IDENTITY_TYPE, "key")
                .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Patch endpoint returns 503 service unavailable when resource changed endpoint cannot connect to CHS Kafka Api")
    void testPatchNewAppointmentCompanyNameStatusApiIllegalArgumentException() throws Exception {

        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(IllegalArgumentException.class);

        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("active");

        mockMvc.perform(patch("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable());
    }
}
