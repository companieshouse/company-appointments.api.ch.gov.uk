package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OfficerAppointmentsControllerITest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2");
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)), "appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Should return HTTP 200 OK and a list of appointments for a particular officer id")
    @Test
    void getOfficerAppointments() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "5VEOBB4a9dlB_iugw_vieHjWpCk")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(35)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is("/officers/5VEOBB4a9dlB_iugw_vieHjWpCk/appointments")))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(2)));
    }

    @DisplayName("Should return HTTP 404 Not Found when no appointments exist for a particular officer id")
    @Test
    void getOfficerAppointmentsNotFound() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "officerId")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "oauth2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("Should return HTTP 401 Unauthorised if the request has no auth headers")
    @Test
    void getOfficerAppointmentsUnauthenticated() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "5VEOBB4a9dlB_iugw_vieHjWpCk")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isUnauthorized());
    }
}
