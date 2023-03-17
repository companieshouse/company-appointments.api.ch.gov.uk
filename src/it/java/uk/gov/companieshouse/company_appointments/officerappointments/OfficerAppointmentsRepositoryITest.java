package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OfficerAppointmentsRepositoryITest {

    private static final String OFFICER_ID = "5VEOBB4a9dlB_iugw_vieHjWpCk";

    @Autowired
    private OfficerAppointmentsRepository repository;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data4.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data5.json", StandardCharsets.UTF_8)), "appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data6.json", StandardCharsets.UTF_8)), "appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Repository returns officer appointments aggregate")
    @Test
    void findOfficerAppointments() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(OFFICER_ID, true);

        // then
        assertEquals(5, officerAppointmentsAggregate.getTotalResults());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(0).getOfficerId());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(1).getOfficerId());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(2).getOfficerId());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(3).getOfficerId());
        assertEquals(OFFICER_ID, officerAppointmentsAggregate.getOfficerAppointments().get(4).getOfficerId());

        assertEquals("active_1",
                officerAppointmentsAggregate.getOfficerAppointments().get(0).getId());
        assertEquals("active_2",
                officerAppointmentsAggregate.getOfficerAppointments().get(1).getId());
        assertEquals("active_3",
                officerAppointmentsAggregate.getOfficerAppointments().get(2).getId());
        assertEquals("resigned_1",
                officerAppointmentsAggregate.getOfficerAppointments().get(3).getId());
        assertEquals("resigned_2",
                officerAppointmentsAggregate.getOfficerAppointments().get(4).getId());
    }

    @DisplayName("Repository returns no appointments when there are no matches")
    @Test
    void findOfficerAppointmentsNoResults() {
        // given
        // when

        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments("officerId", true);

        // then
        assertEquals(0, officerAppointmentsAggregate.getTotalResults());
        assertTrue(officerAppointmentsAggregate.getOfficerAppointments().isEmpty());

    }
}
