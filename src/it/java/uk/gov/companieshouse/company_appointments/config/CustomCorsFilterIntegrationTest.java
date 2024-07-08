
package uk.gov.companieshouse.company_appointments.config;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CustomCorsFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5");
    private static final String DELTA_APPOINTMENTS_COLLECTION = "delta_appointments";
    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection(DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void whenOptionsRequest_thenCorrectHeadersAndStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/officers/{officer_id}/appointments","5VEOBB4a9dlB_iugw_vieHjWpCk")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("Origin","http://www.test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Headers"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Max-Age"));
    }

    @Test
    void whenCorsRequestWithValidMethod_thenProceed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/officers/{officer_id}/appointments","5VEOBB4a9dlB_iugw_vieHjWpCk")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("Origin","http://www.test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenCorsRequestWithInvalidMethod_thenForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/officers/{officer_id}/appointments","5VEOBB4a9dlB_iugw_vieHjWpCk")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("Origin","http://www.test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenCorsRequestWithMissingAllowedOrigin_thenForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/officers/{officer_id}/appointments","5VEOBB4a9dlB_iugw_vieHjWpCk")
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("Origin","http://www.test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
