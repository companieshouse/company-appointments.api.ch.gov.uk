package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
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
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OfficerAppointmentsRepositoryIntegrationTest {

    private static final String OFFICER_ID = "5VEOBB4a9dlB_iugw_vieHjWpCk";

    @Autowired
    private OfficerAppointmentsRepository repository;

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2");

    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)), "appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Repository returns officer appointments aggregate")
    @Test
    void findOfficerAppointments() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(OFFICER_ID);

        // then
        assertEquals(2, officerAppointmentsAggregate.getTotalResults());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(0).getOfficerId());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(1).getOfficerId());
    }

    @DisplayName("Repository returns no appointments when there are no matches")
    @Test
    void findOfficerAppointmentsNoResults() {
        // given
        // when

        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments("officerId");

        // then
        assertEquals(0, officerAppointmentsAggregate.getTotalResults());
        assertTrue(officerAppointmentsAggregate.getOfficerAppointments().isEmpty());

    }
}
