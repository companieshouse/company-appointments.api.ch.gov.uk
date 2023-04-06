package uk.gov.companieshouse.company_appointments.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyAppointmentFullRecordRepositoryITest {

    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String FAKE_APPOINTMENT_ID = "aBCdIdonotexistCD";

    @Autowired
    private CompanyAppointmentFullRecordRepository repository;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)), "delta_appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Repository successfully updates the company name and status of an appointment")
    @Test
    void patchAppointmentNameStatus() {
        // given
        Instant at = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // when
        long result = repository.patchAppointmentNameStatus(APPOINTMENT_ID, "test name", "test status", at, "etag");

        // then
        try {
            assertEquals(1L, result);
            Optional<CompanyAppointmentDocument> actual = repository.findById(APPOINTMENT_ID);
            assertTrue(actual.isPresent());
            assertEquals("test name", actual.get().getCompanyName());
            assertEquals("test status", actual.get().getCompanyStatus());
            assertEquals(at, actual.get().getUpdated().getAt());
            assertEquals("etag", actual.get().getEtag());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("Repository unable to retrieve an existing appointment")
    @Test
    void patchAppointmentNameStatusMissingAppointment() {
        // given
        Instant at = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // when
        long result = repository.patchAppointmentNameStatus(FAKE_APPOINTMENT_ID, "test name", "test status", at, "etag");

        // then
        assertEquals(0L, result);
        Optional<CompanyAppointmentDocument> actual = repository.findById(FAKE_APPOINTMENT_ID);
        assertTrue(actual.isEmpty());
    }
}
