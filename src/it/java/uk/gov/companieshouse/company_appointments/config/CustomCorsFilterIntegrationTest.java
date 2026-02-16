
package uk.gov.companieshouse.company_appointments.config;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CustomCorsFilterIntegrationTest {

    private static final String ERIC_ALLOWED_ORIGIN="ERIC-Allowed-Origin";
    private static final String ERIC_IDENTITY="ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE="ERIC-Identity-Type";
    private static final String ORIGIN="Origin";
    private static final String URL="/officers/{officer_id}/appointments";
    private static final String OFFICER_ID="5VEOBB4a9dlB_iugw_vieHjWpCk";
    private static final String ERIC_ALLOWED_ORIGIN_VALUE="some-origin";
    private static final String ERIC_IDENTITY_VALUE="123";
    private static final String ERIC_IDENTITY_TYPE_VALUE="key";
    private static final String ORIGIN_VALUE="http://www.test.com";

    @Autowired
    private MockMvc mockMvc;
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.2.5");
    private static final String DELTA_APPOINTMENTS_COLLECTION = "delta_appointments";
    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection(DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void whenOptionsRequest_thenCorrectHeadersAndStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options(URL,OFFICER_ID)
                        .header(ERIC_ALLOWED_ORIGIN, ERIC_ALLOWED_ORIGIN_VALUE)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)
                        .header(ORIGIN,ORIGIN_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void whenCorsRequestWithValidMethod_thenProceed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL,OFFICER_ID)
                        .header(ERIC_ALLOWED_ORIGIN, ERIC_ALLOWED_ORIGIN_VALUE)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)
                        .header(ORIGIN,ORIGIN_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenCorsRequestWithInvalidMethod_thenForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL,OFFICER_ID)
                        .header(ERIC_ALLOWED_ORIGIN, ERIC_ALLOWED_ORIGIN_VALUE)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)
                        .header(ORIGIN,ORIGIN_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenCorsRequestWithMissingAllowedOrigin_thenForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL,OFFICER_ID)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)
                        .header(ORIGIN,ORIGIN_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
