package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
@TestPropertySource(properties = {"logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG"})
class OfficerAppointmentsRepositoryITest {

    private static final String OFFICER_ID = "5VEOBB4a9dlB_iugw_vieHjWpCk";
    private static final int START_INDEX = 0;
    private static final int DEFAULT_ITEMS_PER_PAGE = 35;
    private static final String REMOVED = "removed";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private static final String DISSOLVED = "dissolved";
    private static final List<String> FILTER_STATUSES = List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED);

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5");
    private static final String MISSING_OFFICER_ID = "missing";
    @Autowired
    private OfficerAppointmentsRepository repository;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");

        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data7.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data6.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data5.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data4.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");

        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Repository returns sorted officer appointments IDs")
    @Test
    void findOfficerAppointmentsIds() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds(OFFICER_ID, false, emptyList(),
                START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(7, appointmentsIds.getIds().size());
        assertEquals("active_appointed_on_1", appointmentsIds.getIds().get(0));
        assertEquals("active_appointed_on_2", appointmentsIds.getIds().get(1));
        assertEquals("active_appointed_before_1", appointmentsIds.getIds().get(2));
        assertEquals("dissolved_appointed_before_1", appointmentsIds.getIds().get(3));
        assertEquals("active_appointed_before_2", appointmentsIds.getIds().get(4));
        assertEquals("active_resigned_on_1", appointmentsIds.getIds().get(5));
        assertEquals("active_resigned_on_2", appointmentsIds.getIds().get(6));
    }

    @DisplayName("Repository returns no appointments IDs when there are no matches")
    @Test
    void findOfficerAppointmentsIdsNoResults() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds("officerId", false, emptyList(),
                START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertTrue(appointmentsIds.getIds().isEmpty());
    }

    @DisplayName("Repository returns only active appointments IDs when the filter is enabled")
    @Test
    void findActiveOfficerAppointmentsIds() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds(OFFICER_ID, true,
                FILTER_STATUSES, START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(4, appointmentsIds.getIds().size());
        assertEquals("active_appointed_on_1", appointmentsIds.getIds().get(0));
        assertEquals("active_appointed_on_2", appointmentsIds.getIds().get(1));
        assertEquals("active_appointed_before_1", appointmentsIds.getIds().get(2));
        assertEquals("active_appointed_before_2", appointmentsIds.getIds().get(3));
    }

    @DisplayName("Repository returns no appointments IDs when there are no matches when the filter is enabled")
    @Test
    void findActiveOfficerAppointmentsIdsNoResults() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds("officerId", true, emptyList(),
                START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertTrue(appointmentsIds.getIds().isEmpty());
    }

    @DisplayName("Repository returns a paged list of officer appointments IDs")
    @Test
    void findOfficerAppointmentsIdsWithPaging() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds(OFFICER_ID, false, emptyList(), 1,
                5);

        // then
        assertEquals(5, appointmentsIds.getIds().size());
        assertEquals("active_appointed_on_2", appointmentsIds.getIds().get(0));
        assertEquals("active_appointed_before_1", appointmentsIds.getIds().get(1));
        assertEquals("dissolved_appointed_before_1", appointmentsIds.getIds().get(2));
        assertEquals("active_appointed_before_2", appointmentsIds.getIds().get(3));
        assertEquals("active_resigned_on_1", appointmentsIds.getIds().get(4));
    }

    @DisplayName("Repository returns a paged list of officer appointments IDs with the filter applied")
    @Test
    void findActiveOfficerAppointmentsIdsWithPaging() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds(OFFICER_ID, true,
                FILTER_STATUSES, 1, 3);

        // then
        assertEquals(3, appointmentsIds.getIds().size());
        assertEquals("active_appointed_on_2", appointmentsIds.getIds().get(0));
        assertEquals("active_appointed_before_1", appointmentsIds.getIds().get(1));
        assertEquals("active_appointed_before_2", appointmentsIds.getIds().get(2));
    }

    @DisplayName("Repository returns no officer appointments IDs when start index is greater than total matches")
    @Test
    void findOfficerAppointmentsIdsHighStartIndex() {
        // given

        // when
        OfficerAppointments appointmentsIds = repository.findOfficerAppointmentsIds(OFFICER_ID, false, emptyList(), 10,
                DEFAULT_ITEMS_PER_PAGE);

        // then
        assertTrue(appointmentsIds.getIds().isEmpty());
    }

    @DisplayName("Repository returns officer appointments with order preserved given a sorted list of IDs")
    @Test
    void findFullOfficerAppointments() {
        // given
        List<String> appointmentsIds = repository.findOfficerAppointmentsIds(OFFICER_ID, false, emptyList(),
                        START_INDEX, DEFAULT_ITEMS_PER_PAGE)
                .getIds();

        // when
        List<CompanyAppointmentDocument> documents = repository.findFullOfficerAppointments(appointmentsIds);

        // then
        assertEquals(7, documents.size());
        assertEquals(appointmentsIds, documents.stream().map(CompanyAppointmentDocument::getId).toList());
    }

    @DisplayName("Repository returns count of total officer appointments")
    @Test
    void countTotal() {
        // given

        // when
        int total = repository.countTotal(OFFICER_ID, false, emptyList());

        // then
        assertEquals(7, total);
    }

    @DisplayName("Repository returns count of zero total officer appointments")
    @Test
    void countTotalZero() {
        // given

        // when
        int total = repository.countTotal(MISSING_OFFICER_ID, false, emptyList());

        // then
        assertEquals(0, total);
    }

    @DisplayName("Repository returns count of total active officer appointments")
    @Test
    void countActiveTotal() {
        // given

        // when
        int total = repository.countTotal(OFFICER_ID, true, FILTER_STATUSES);

        // then
        assertEquals(4, total);
    }

    @DisplayName("Repository returns count of zero total active officer appointments")
    @Test
    void countActiveTotalZero() {
        // given

        // when
        int total = repository.countTotal(MISSING_OFFICER_ID, true, FILTER_STATUSES);

        // then
        assertEquals(0, total);
    }

    @DisplayName("Repository returns count of resigned officer appointments")
    @Test
    void countResigned() {
        // given

        // when
        int resigned = repository.countResigned(OFFICER_ID);

        // then
        assertEquals(2, resigned);
    }

    @DisplayName("Repository returns count of zero resigned officer appointments")
    @Test
    void countResignedZero() {
        // given

        // when
        int resigned = repository.countResigned(MISSING_OFFICER_ID);

        // then
        assertEquals(0, resigned);
    }

    @DisplayName("Repository returns count of inactive officer appointments")
    @Test
    void countInactive() {
        // given

        // when
        int resigned = repository.countInactive(OFFICER_ID);

        // then
        assertEquals(1, resigned);
    }

    @DisplayName("Repository returns count of zero inactive officer appointments")
    @Test
    void countInactiveZero() {
        // given

        // when
        int resigned = repository.countInactive(MISSING_OFFICER_ID);

        // then
        assertEquals(0, resigned);
    }

    @DisplayName("Repository should return the first appointment for the given officer ID sorted by appointed on")
    @Test
    void findFirstByOfficerId() {
        // given

        // when
        CompanyAppointmentDocument actual = repository.findLatestAppointment(OFFICER_ID);

        // then
        assertEquals("active_appointed_on_1", actual.getId());
    }

    @DisplayName("Repository should return no appointment for the given officer ID")
    @Test
    void findFirstByOfficerIdEmpty() {
        // given

        // when
        CompanyAppointmentDocument actual = repository.findLatestAppointment("not an officer id");

        // then
        assertNull(actual);
    }

    @DisplayName("Repository returns officer appointments sorted to appointed_on and appointed_before dates descending")
    @Test
    void findOfficerAppointments() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments(OFFICER_ID, false, emptyList(),
                START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(7, appointments.size());
        assertEquals("active_appointed_on_1", appointments.get(0).getId());
        assertEquals("active_appointed_on_2", appointments.get(1).getId());
        assertEquals("active_resigned_on_1", appointments.get(2).getId());
        assertEquals("active_appointed_before_1", appointments.get(3).getId());
        // there is business logic wherein in live an appointed_on can be before an appointed_before.
        assertEquals("active_resigned_on_2", appointments.get(4).getId());
        assertEquals("dissolved_appointed_before_1", appointments.get(5).getId());
        assertEquals("active_appointed_before_2", appointments.get(6).getId());
    }

    @DisplayName("Repository returns no appointments when there are no matches")
    @Test
    void findOfficerAppointmentsNoResults() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments("officerId", false,
                emptyList(),
                START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertTrue(appointments.isEmpty());
    }

    @DisplayName("Repository returns only active sorted appointments when the filter is enabled")
    @Test
    void findActiveOfficerAppointments() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments(OFFICER_ID, true,
                FILTER_STATUSES, START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertEquals(4, appointments.size());
        assertEquals("active_appointed_on_1", appointments.get(0).getId());
        assertEquals("active_appointed_on_2", appointments.get(1).getId());
        assertEquals("active_appointed_before_1", appointments.get(2).getId());
        assertEquals("active_appointed_before_2", appointments.get(3).getId());
    }

    @DisplayName("Repository returns no unsorted appointments when there are no matches when the filter is enabled")
    @Test
    void findActiveOfficerAppointmentsUnsortedNoResults() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments("officerId", true, emptyList(),
                START_INDEX, DEFAULT_ITEMS_PER_PAGE);

        // then
        assertTrue(appointments.isEmpty());
    }

    @DisplayName("Repository returns a paged list of sorted officer appointments")
    @Test
    void findOfficerAppointmentsWithPaging() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments(OFFICER_ID, false, emptyList(),
                1,
                4);

        // then
        assertEquals(4, appointments.size());
        assertEquals("active_resigned_on_1", appointments.get(0).getId());
        assertEquals("active_appointed_on_2", appointments.get(1).getId());
        assertEquals("active_appointed_before_1", appointments.get(2).getId());
        assertEquals("active_resigned_on_2", appointments.get(3).getId());
    }

    @DisplayName("Repository returns a paged list of sorted officer appointments with the filter applied")
    @Test
    void findActiveOfficerAppointmentsWithPaging() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments(OFFICER_ID, true,
                FILTER_STATUSES, 1, 3);

        // then
        assertEquals(3, appointments.size());
        assertEquals("active_appointed_on_2", appointments.get(0).getId());
        assertEquals("active_appointed_before_1", appointments.get(1).getId());
        assertEquals("active_appointed_before_2", appointments.get(2).getId());
    }

    @DisplayName("Repository returns no unsorted officer appointments when start index is greater than total matches")
    @Test
    void findOfficerAppointmentsHighStartIndex() {
        // given

        // when
        List<CompanyAppointmentDocument> appointments = repository.findOfficerAppointments(OFFICER_ID, false, emptyList(),
                10,
                DEFAULT_ITEMS_PER_PAGE);

        // then
        assertTrue(appointments.isEmpty());
    }
}
