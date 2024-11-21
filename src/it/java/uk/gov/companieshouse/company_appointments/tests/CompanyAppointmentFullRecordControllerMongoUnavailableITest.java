package uk.gov.companieshouse.company_appointments.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompanyAppointmentFullRecordControllerMongoUnavailableITest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String X_DELTA_AT = "x-delta-at";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    private static final String FULL_RECORD_DELETE_ENDPOINT = "/company/{company_number}/appointments/{appointment_id}/full_record";
    private static final String FULL_RECORD_PUT_ENDPOINT = "/company/{company_number}/appointments/{appointment_id}/full_record";
    private static final String DELTA_AT = "20140925171003950844";
    private static final String OFFICER_ID = "officer_id";
    private static final String X_OFFICER_ID = "x-officer-id";

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
    @DisplayName("Put endpoint returns 503 service unavailable when MongoDB is unavailable")
    void testPutNewAppointmentCompanyNameStatusMongoUnavailable() throws Exception {
        doThrow(new DataAccessException("..."){ }).when(fullRecordRepository).save(any());

        FullRecordCompanyOfficerApi requestBody = new FullRecordCompanyOfficerApi()
                .externalData(new ExternalData().companyNumber(COMPANY_NUMBER));

        mockMvc.perform(put(FULL_RECORD_PUT_ENDPOINT, COMPANY_NUMBER, APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Delete endpoint returns 502 BadGateway when MongoDB is unavailable")
    void testDeleteNewAppointmentCompanyNameStatusMongoUnavailable() throws Exception {
        when(fullRecordRepository.readByCompanyNumberAndID(any(), any())).thenThrow(new DataAccessException("..."){ });
        
        mockMvc.perform(delete(FULL_RECORD_DELETE_ENDPOINT, COMPANY_NUMBER, APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .header(X_DELTA_AT, DELTA_AT)
                        .header(X_OFFICER_ID, OFFICER_ID))
                .andExpect(status().isBadGateway());
    }
}
