package uk.gov.companieshouse.company_appointments;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CompanyAppointmentFullRecordControllerITest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String OFFICER_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2");

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
    void testReturn200IfOfficerIsDeleted() throws Exception{
        ResultActions result = mockMvc
                .perform(delete("/company/{company_number}/appointments/{appointment_id}/full_record/delete", COMPANY_NUMBER,
                        OFFICER_ID)
                        .header("ERIC-Identity", "123").header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "internal-app"));

        result.andExpect(status().isOk());

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("7IjxamNGLlqtIingmTZJJ42Hw9Q"));
        List<DeltaAppointmentApiEntity> appointments = mongoTemplate.find(query, DeltaAppointmentApiEntity.class);
        assertThat(appointments, is(empty()));
    }

    @Test
    void testReturn404IfOfficerIsNotDeleted() throws Exception{
        ResultActions result = mockMvc
                .perform(delete("/company/{company_number}/appointments/{appointment_id}/full_record/delete", COMPANY_NUMBER,
                        "Incorrect")
                        .header("ERIC-Identity", "123").header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "internal-app"));

        result.andExpect(status().isNotFound());

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("7IjxamNGLlqtIingmTZJJ42Hw9Q"));
        List<DeltaAppointmentApiEntity> appointments = mongoTemplate.find(query, DeltaAppointmentApiEntity.class);
        assertThat(appointments.size(), is(1));
    }
}
