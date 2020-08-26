package uk.gov.companieshouse.company_appointments;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CompanyAppointmentControllerITest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:3.6");

    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDbFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "appointments");
    }

    //TODO:
    // HTTP 404 Not Found if officer doesn't exist
    // HTTP 401 if user not authenticated
    // HTTP 500 if database unavailable

    @Test
    void testReturn200OKIfOfficerIsFound() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/company/{company_number}/appointments/{appointment_id}", "12345678", "7IjxamNGLlqtIingmTZJJ42Hw9Q")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "api")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)));
    }
}
