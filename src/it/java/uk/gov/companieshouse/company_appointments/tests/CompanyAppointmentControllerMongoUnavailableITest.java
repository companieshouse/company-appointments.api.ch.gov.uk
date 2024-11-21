package uk.gov.companieshouse.company_appointments.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyAppointmentControllerMongoUnavailableITest {

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

    @MockBean
    private CompanyAppointmentRepository fullRecordRepository;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    @DisplayName("Patch endpoint returns 503 service unavailable when MongoDB is unavailable")
    void testPatchNewAppointmentCompanyNameStatusMongoUnavailable() throws Exception {
        when(fullRecordRepository.existsById(any())).thenReturn(true);
        when(fullRecordRepository.patchAppointmentNameStatusInCompany(any(), any(), any(), any(), any())).thenThrow(DataAccessResourceFailureException.class);

        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("active");

        mockMvc.perform(patch("/company/{company_number}/appointments", COMPANY_NUMBER, APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isServiceUnavailable());
    }
}
