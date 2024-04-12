package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.result.DeleteResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Testcontainers
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PerformanceITest {

    private static final String LOCAL_MONGO_CONNECTION = "mongodb://localhost:27017/appointments?retryWrites=false&loadBalanced=false&serverSelectionTimeoutMS=5000&connectTimeoutMS=10000&3t.uriVersion=3&3t.connection.name=Local+-+Tilt&3t.alwaysShowAuthDB=true&3t.alwaysShowDBFromUserRole=true";
    private static final String DELTA_APPOINTMENTS_COLLECTION = "delta_appointments";
    private static MongoTemplate mongoTemplate;

    @Autowired
    private OfficerAppointmentsRepository repository;
    @Autowired
    private OfficerAppointmentsService service;

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", LOCAL_MONGO_CONNECTION);
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(LOCAL_MONGO_CONNECTION));
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void officerAppointmentsPerformanceTests() throws Exception {
        // given
        final String officerId = UUID.randomUUID().toString();
        final int appointmentCount = 50_000;

        try {
            createOfficer(officerId, appointmentCount);

            Pair<Long, OfficerAppointmentsAggregate> pairOneHit = oneHitFetch(officerId);
            Pair<Long, OfficerAppointmentsAggregate> pairTwoHit = twoHitFetch(officerId);
            Pair<Long, OfficerAppointmentsAggregate> pairTwoHitORSO = twoHitFetchOfficerRoleSortOrder(officerId);
            Pair<Long, OfficerAppointmentsAggregate> fourHitORSO = fourHitFetchOfficerRoleSortOrder(officerId);

            System.out.printf(
                    "\nOne hit: %d\nTwo hit: %d\nTwo hit officer role sort order: %d\nFour hit officer role sort order: %d",
                    pairOneHit.getFirst(), pairTwoHit.getFirst(), pairTwoHitORSO.getFirst(), fourHitORSO.getFirst());
            assertEquals(pairTwoHit.getSecond().getTotalResults(), pairTwoHitORSO.getSecond().getTotalResults());
            assertEquals(pairTwoHit.getSecond().getResignedCount(),
                    pairTwoHitORSO.getSecond().getResignedCount());
            assertEquals(pairTwoHit.getSecond().getInactiveCount(), pairTwoHitORSO.getSecond().getInactiveCount());

            assertEquals(pairTwoHitORSO.getSecond().getTotalResults(), fourHitORSO.getSecond().getTotalResults());
            assertEquals(pairTwoHitORSO.getSecond().getResignedCount(),
                    fourHitORSO.getSecond().getResignedCount());
            assertEquals(pairTwoHitORSO.getSecond().getInactiveCount(), fourHitORSO.getSecond().getInactiveCount());
        } finally {
            // clean up
            Query query = new Query()
                    .addCriteria(Criteria.where("officer_id").is(officerId));
            DeleteResult result = mongoTemplate.remove(query, DELTA_APPOINTMENTS_COLLECTION);
            assertEquals(appointmentCount, result.getDeletedCount());
            assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
        }
    }

    private Pair<Long, OfficerAppointmentsAggregate> fourHitFetchOfficerRoleSortOrder(String officerId) {
        Instant start = Instant.now();
        try {
            OfficerAppointmentsAggregate twoHit = service.findOfficerSeparateCalls(officerId,
                    new Filter(false, List.of()), 0, 500);
            return Pair.of(Duration.between(start, Instant.now()).toMillis(), twoHit);
        } catch (Exception e) {
            System.out.println("fourHit failed");
        }
        return Pair.of(0L, new OfficerAppointmentsAggregate());
    }

    private Pair<Long, OfficerAppointmentsAggregate> twoHitFetchOfficerRoleSortOrder(String officerId) {
        Instant start = Instant.now();
        try {
            OfficerAppointmentsAggregate twoHit = service.findOfficerOfficerRoleSortOrder(officerId,
                    new Filter(false, List.of()), 0, 500);
            return Pair.of(Duration.between(start, Instant.now()).toMillis(), twoHit);
        } catch (Exception e) {
            System.out.println("twoHitORSO failed");
        }
        return Pair.of(0L, new OfficerAppointmentsAggregate());
    }

    private Pair<Long, OfficerAppointmentsAggregate> twoHitFetch(String officerId) {
        Instant start = Instant.now();
        try {
            OfficerAppointmentsAggregate twoHit = service.findOfficerWithLargeAppointmentsCount(
                    officerId,
                    new Filter(false, List.of()), 0, 500);
            return Pair.of(Duration.between(start, Instant.now()).toMillis(), twoHit);
        } catch (Exception e) {
            System.out.println("twoHit failed");
        }
        return Pair.of(0L, new OfficerAppointmentsAggregate());
    }

    private Pair<Long, OfficerAppointmentsAggregate> oneHitFetch(String officerId) {
        Instant start = Instant.now();
        try {
            OfficerAppointmentsAggregate oneHit = repository.findOfficerAppointments(officerId, false, List.of(), 0,
                    500);
            return Pair.of(Duration.between(start, Instant.now()).toMillis(), oneHit);
        } catch (Exception e) {
            System.out.println("oneHit failed");
        }
        return Pair.of(0L, new OfficerAppointmentsAggregate());
    }

    private static void createOfficer(String officerId, int appointmentCount)
            throws IOException {

        System.out.printf("Creating officer_id: \"%s\"", officerId);

        String rawJson = IOUtils.resourceToString("/internal-appointment-data.json",
                StandardCharsets.UTF_8);

        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < appointmentCount; i++) {
            Document document = Document.parse(rawJson
                    .replaceAll("<id>", UUID.randomUUID().toString())
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);
    }
}