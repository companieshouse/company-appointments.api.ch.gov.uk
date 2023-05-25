package uk.gov.companieshouse.company_appointments.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    private static final String APPOINTMENT_ID_2 = "app2";
    private static final String APPOINTMENT_ID_3 = "app3";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NUMBER_2 = "anotherCompanyNumber";
    private static final String FAKE_APPOINTMENT_ID = "aBCdIdonotexistCD";
    private static final String INITIAL_APPOINTMENT_ID = "fedb91fa70ce4ef335d6d2e24f2f7242c9360a69";

    @Autowired
    private CompanyAppointmentFullRecordRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @BeforeEach
    void setup() throws IOException {
        mongoTemplate.dropCollection("delta_appointments");
        mongoTemplate.createCollection("delta_appointments");
        Document document = Document.parse(
                IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8));
        mongoTemplate.insert(document, "delta_appointments");

        document.put("appointment_id", APPOINTMENT_ID_2);
        document.put("_id", APPOINTMENT_ID_2);
        mongoTemplate.insert(document, "delta_appointments");

        document.put("appointment_id", APPOINTMENT_ID_3);
        document.put("_id", APPOINTMENT_ID_3);
        document.put("company_number", COMPANY_NUMBER_2);
        mongoTemplate.insert(document, "delta_appointments");
    }

    @DisplayName("Repository successfully updates the company name and status of an appointment")
    @Test
    void patchAppointmentNameStatus() {
        // given
        Instant at = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // when
        long result = repository.patchAppointmentNameStatus(APPOINTMENT_ID, "test name",
                "test status", at, "etag");

        // then
        try {
            assertEquals(1L, result);
            Optional<CompanyAppointmentDocument> actual = repository.findById(APPOINTMENT_ID);
            assertTrue(actual.isPresent());
            assertEquals("test name", actual.get().getCompanyName());
            assertEquals("test status", actual.get().getCompanyStatus());
            assertEquals(at, actual.get().getUpdated().getAt());
            assertEquals("etag", actual.get().getData().getEtag());
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
        long result = repository.patchAppointmentNameStatus(FAKE_APPOINTMENT_ID, "test name",
                "test status", at, "etag");

        // then
        assertEquals(0L, result);
        Optional<CompanyAppointmentDocument> actual = repository.findById(FAKE_APPOINTMENT_ID);
        assertTrue(actual.isEmpty());
    }

    @DisplayName("Repository successfully updates the company name and status for all appointments in company")
    @Test
    void shouldPatchAllAppointmentsNameStatusInCompany() {
        // given
        Instant at = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // when
        long result = repository.patchAppointmentNameStatusInCompany(COMPANY_NUMBER, "test name",
                "test status", at, "etag");

        // then
        assertEquals(2L, result);

        List.of(APPOINTMENT_ID, APPOINTMENT_ID_2).forEach(id -> {
            Optional<CompanyAppointmentDocument> actual = repository.findById(id);
            assertTrue(actual.isPresent());
            assertEquals("test name", actual.get().getCompanyName());
            assertEquals("test status", actual.get().getCompanyStatus());
            assertEquals(at, actual.get().getUpdated().getAt());
            assertEquals("etag", actual.get().getData().getEtag());
        });

        Optional<CompanyAppointmentDocument> actual = repository.findById(APPOINTMENT_ID_3);
        assertTrue(actual.isPresent());
        assertEquals(INITIAL_APPOINTMENT_ID, actual.get().getData().getEtag());
    }

    @DisplayName("Repository returns true when appointment exists")
    @Test
    void existsByIdTrue() {
        // given
        // when
        boolean actual = repository.existsById(APPOINTMENT_ID);

        // then
        assertTrue(actual);
    }

    @DisplayName("Repository returns false when appointment does not exist")
    @Test
    void existsByIdFalse() {
        // given
        // when
        boolean actual = repository.existsById(FAKE_APPOINTMENT_ID);

        // then
        assertFalse(actual);
    }
}
