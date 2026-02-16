package uk.gov.companieshouse.company_appointments.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyAppointmentRepositoryITest {

    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String APPOINTMENT_ID_2 = "app2";
    private static final String APPOINTMENT_ID_3 = "app3";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NUMBER_2 = "anotherCompanyNumber";
    private static final String FAKE_APPOINTMENT_ID = "aBCdIdonotexistCD";

    @Autowired
    private CompanyAppointmentRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.2.5");

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

    @DisplayName("Repository returns document with given company number and appointment ID")
    @Test
    void readByCompanyNumberAndAppointmentID() {
        // given
        // when
        Optional<CompanyAppointmentDocument> actual = repository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER,
                APPOINTMENT_ID);

        // then
        assertTrue(actual.isPresent());
    }
}
