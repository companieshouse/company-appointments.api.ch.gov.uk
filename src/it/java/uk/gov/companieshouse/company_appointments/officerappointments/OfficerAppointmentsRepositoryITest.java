package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = { "logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG" })
class OfficerAppointmentsRepositoryITest {

    private static final String OFFICER_ID = "5VEOBB4a9dlB_iugw_vieHjWpCk";
    private static final String SECOND_OFFICER_ID = "1234";
    private static final int START_INDEX = 0;
    private static final int DEFAULT_ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE = 50;
    private static final int SECOND_OFFICER_TOTAL_RESULTS = 55;
    private static final String REMOVED = "removed";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private static final String DISSOLVED = "dissolved";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");
    @Autowired
    private OfficerAppointmentsRepository repository;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");

        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data4.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data5.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data6.json", StandardCharsets.UTF_8)), "delta_appointments");

        // Adding over 50 appointments for a second officer to test pagination works
        for (int i = 0; i < SECOND_OFFICER_TOTAL_RESULTS; i++) {
            CompanyAppointmentDocument appointment = new CompanyAppointmentDocument();
            appointment.officerId(SECOND_OFFICER_ID);
            mongoTemplate.insert(appointment);
        }
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Repository returns officer appointments aggregate")
    @Test
    void findOfficerAppointments() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(OFFICER_ID, false, emptyList(), START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(5, officerAppointmentsAggregate.getTotalResults());
        assertEquals(1, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(2, officerAppointmentsAggregate.getResignedCount());

        assertEquals("active_1",
                officerAppointmentsAggregate.getOfficerAppointments().get(0).getId());
        assertEquals("active_2",
                officerAppointmentsAggregate.getOfficerAppointments().get(1).getId());
        assertEquals("dissolved_1",
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
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments("officerId", false, emptyList(), START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(0, officerAppointmentsAggregate.getTotalResults());
        assertEquals(0, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(0, officerAppointmentsAggregate.getResignedCount());
        assertTrue(officerAppointmentsAggregate.getOfficerAppointments().isEmpty());

    }

    @DisplayName("Repository returns only active appointments when the filter is enabled")
    @Test
    void findActiveOfficerAppointments() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(
                OFFICER_ID,
                true,
                List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED),
                START_INDEX,
                DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(2, officerAppointmentsAggregate.getTotalResults());
        assertEquals(0, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(0, officerAppointmentsAggregate.getResignedCount());

        assertEquals("active_1",
                officerAppointmentsAggregate.getOfficerAppointments().get(0).getId());
        assertEquals("active_2",
                officerAppointmentsAggregate.getOfficerAppointments().get(1).getId());
    }

    @DisplayName("Repository returns no appointments when there are no matches when the filter is enabled")
    @Test
    void findActiveOfficerAppointmentsNoResults() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments("officerId", true, emptyList(), START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(0, officerAppointmentsAggregate.getTotalResults());
        assertEquals(0, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(0, officerAppointmentsAggregate.getResignedCount());

        assertTrue(officerAppointmentsAggregate.getOfficerAppointments().isEmpty());
    }

    @DisplayName("Repository returns a paged officer appointments aggregate")
    @Test
    void findOfficerAppointmentsWithPaging() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(OFFICER_ID, false, emptyList(), 1, 3);

        // then
        assertEquals(5, officerAppointmentsAggregate.getTotalResults());
        assertEquals(1, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(2, officerAppointmentsAggregate.getResignedCount());

        assertEquals("active_2",
                officerAppointmentsAggregate.getOfficerAppointments().get(0).getId());
        assertEquals("dissolved_1",
                officerAppointmentsAggregate.getOfficerAppointments().get(1).getId());
        assertEquals("resigned_1",
                officerAppointmentsAggregate.getOfficerAppointments().get(2).getId());
    }

    @DisplayName("Repository returns a paged officer appointments aggregate with the filter applied")
    @Test
    void findOfficerAppointmentsWithPagingAndFilter() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(
                OFFICER_ID, true, List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED), 1, 3);

        // then
        assertEquals(2, officerAppointmentsAggregate.getTotalResults());
        assertEquals(0, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(0, officerAppointmentsAggregate.getResignedCount());

        assertEquals("active_2",
                officerAppointmentsAggregate.getOfficerAppointments().get(0).getId());
    }

    @DisplayName("Repository returns a no officer appointments when start index is greater than total results")
    @Test
    void findOfficerAppointmentsIncorrectStartIndex() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(OFFICER_ID, false, emptyList(), 10, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(5, officerAppointmentsAggregate.getTotalResults());
        assertEquals(1, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(2, officerAppointmentsAggregate.getResignedCount());
        assertTrue(officerAppointmentsAggregate.getOfficerAppointments().isEmpty());
    }

    @DisplayName("Repository returns a paged officer appointments aggregate with correct total results items per page is set to over 50")
    @Test
    void findOfficerAppointmentsWithPagingOver50() {
        // given

        // when
        OfficerAppointmentsAggregate officerAppointmentsAggregate = repository.findOfficerAppointments(SECOND_OFFICER_ID, false, emptyList(), START_INDEX, MAX_ITEMS_PER_PAGE);

        // then
        assertEquals(SECOND_OFFICER_TOTAL_RESULTS, officerAppointmentsAggregate.getTotalResults());
        assertEquals(0, officerAppointmentsAggregate.getInactiveCount());
        assertEquals(0, officerAppointmentsAggregate.getResignedCount());
        assertEquals(MAX_ITEMS_PER_PAGE, officerAppointmentsAggregate.getOfficerAppointments().size());
    }

    @DisplayName("Repository should return the first appointment for the given officer ID")
    @Test
    void findFirstByOfficerId() {
        // given

        // when
        Optional<CompanyAppointmentDocument> actual = repository.findFirstByOfficerId(OFFICER_ID);

        // then
        assertTrue(actual.isPresent());
        assertEquals(OFFICER_ID, actual.get().getOfficerId());
    }

    @DisplayName("Repository should return no appointment for the given officer ID")
    @Test
    void findFirstByOfficerIdEmpty() {
        // given

        // when
        Optional<CompanyAppointmentDocument> actual = repository.findFirstByOfficerId("not an officer id");

        // then
        assertTrue(actual.isEmpty());
    }
}
