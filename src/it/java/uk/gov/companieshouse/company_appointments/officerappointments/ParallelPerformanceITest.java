package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.util.Pair;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

@Testcontainers
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ParallelPerformanceITest {

    private static final String LOCAL_MONGO_CONNECTION = "mongodb://localhost:27017/appointments?retryWrites=false&loadBalanced=false&serverSelectionTimeoutMS=5000&connectTimeoutMS=10000";
    private static final String CIDEV_MONGO_CONNECTION = "redacted";
    private static final String MONGO_CONNECTION = CIDEV_MONGO_CONNECTION;

    private static final String DELTA_APPOINTMENTS_COLLECTION = "delta_appointments";
    private static MongoTemplate mongoTemplate;

    @Autowired
    private OfficerAppointmentsRepository repository;
    @Autowired
    private OfficerAppointmentsService service;

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", MONGO_CONNECTION);
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(MONGO_CONNECTION));
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    static class Timings {

        final String officerId;
        final long twoHitFetch;
        final long twoHitFetchOfficerRoleSortOrder;
        final long fourHitFetchOfficerRoleSortOrder;
        final long fiveHitFetch;

        Timings(String officerId, long twoHitFetch, long twoHitFetchOfficerRoleSortOrder,
                long fourHitFetchOfficerRoleSortOrder, long fiveHitFetch) {
            this.officerId = officerId;
            this.twoHitFetch = twoHitFetch;
            this.twoHitFetchOfficerRoleSortOrder = twoHitFetchOfficerRoleSortOrder;
            this.fourHitFetchOfficerRoleSortOrder = fourHitFetchOfficerRoleSortOrder;
            this.fiveHitFetch = fiveHitFetch;
        }

        public String getOfficerId() {
            return officerId;
        }

        public long getTwoHitFetch() {
            return twoHitFetch;
        }

        public long getTwoHitFetchOfficerRoleSortOrder() {
            return twoHitFetchOfficerRoleSortOrder;
        }

        public long getFourHitFetchOfficerRoleSortOrder() {
            return fourHitFetchOfficerRoleSortOrder;
        }

        public long getFiveHitFetch() {
            return fiveHitFetch;
        }
    }

    @Test
    void parallelPerformanceTests() {
        // given
        final int appointmentCount = 120_000;
//        List<String> officerIds = IntStream.range(0, 10)
//                .mapToObj((i) -> {
//                    String officerId = UUID.randomUUID().toString();
//                    System.out.printf("\nCreating officer %d, ID %s", i, officerId);
//                    createOfficer(officerId, appointmentCount);
//                    return officerId;
//                })
//                .collect(Collectors.toList());

//        Creating officer 0, ID 8906ab29-55dd-45f2-a4d5-29c9e77d7c71
//        Creating officer 1, ID f226c3bb-eee1-4ef8-8016-67a18e68f093
//        Creating officer 2, ID 191ee01b-8bad-4dc1-a2db-15b92c84aec9
//        Creating officer 3, ID 772eb110-c6c6-460d-88b7-142455aa5dd8
//        Creating officer 4, ID 0b44ca22-21e8-4e71-bffc-6d60245d41ae
//        Creating officer 5, ID 08c8fa55-19bf-4297-90f9-01851911bb97
//        Creating officer 6, ID c5101570-cca5-43f3-a496-107f36a5ebbf
//        Creating officer 7, ID a5202d1c-112e-4c49-8603-ca29c8425107
//        Creating officer 8, ID 57390c3a-a48d-4c48-b42f-52218cfe0adf
//        Creating officer 9, ID 793fe5aa-e153-432a-8b02-4d680b738113

        List<String> officerIds = List.of(
                "8906ab29-55dd-45f2-a4d5-29c9e77d7c71",
                "f226c3bb-eee1-4ef8-8016-67a18e68f093",
                "191ee01b-8bad-4dc1-a2db-15b92c84aec9",
                "772eb110-c6c6-460d-88b7-142455aa5dd8",
                "0b44ca22-21e8-4e71-bffc-6d60245d41ae",
                "08c8fa55-19bf-4297-90f9-01851911bb97",
                "c5101570-cca5-43f3-a496-107f36a5ebbf",
                "a5202d1c-112e-4c49-8603-ca29c8425107",
                "57390c3a-a48d-4c48-b42f-52218cfe0adf",
                "793fe5aa-e153-432a-8b02-4d680b738113",
                "8906ab29-55dd-45f2-a4d5-29c9e77d7c71",
                "f226c3bb-eee1-4ef8-8016-67a18e68f093",
                "191ee01b-8bad-4dc1-a2db-15b92c84aec9",
                "772eb110-c6c6-460d-88b7-142455aa5dd8",
                "0b44ca22-21e8-4e71-bffc-6d60245d41ae",
                "08c8fa55-19bf-4297-90f9-01851911bb97",
                "c5101570-cca5-43f3-a496-107f36a5ebbf",
                "a5202d1c-112e-4c49-8603-ca29c8425107",
                "57390c3a-a48d-4c48-b42f-52218cfe0adf",
                "793fe5aa-e153-432a-8b02-4d680b738113",
                "8906ab29-55dd-45f2-a4d5-29c9e77d7c71",
                "f226c3bb-eee1-4ef8-8016-67a18e68f093",
                "191ee01b-8bad-4dc1-a2db-15b92c84aec9",
                "772eb110-c6c6-460d-88b7-142455aa5dd8",
                "0b44ca22-21e8-4e71-bffc-6d60245d41ae",
                "08c8fa55-19bf-4297-90f9-01851911bb97",
                "c5101570-cca5-43f3-a496-107f36a5ebbf",
                "a5202d1c-112e-4c49-8603-ca29c8425107",
                "57390c3a-a48d-4c48-b42f-52218cfe0adf",
                "793fe5aa-e153-432a-8b02-4d680b738113");

        List<Timings> timings;
        try {
            timings = officerIds.stream()
                    .parallel()
                    .map(officerId -> {
                        try {
                            Pair<Long, OfficerAppointmentsAggregate> pairTwoHit = twoHitFetch(officerId);
                            Pair<Long, OfficerAppointmentsAggregate> pairTwoHitORSO = twoHitFetchOfficerRoleSortOrder(
                                    officerId);
                            Pair<Long, OfficerAppointmentsAggregate> fourHitORSO = fourHitFetchOfficerRoleSortOrder(
                                    officerId);
                            Pair<Long, OfficerAppointmentsAggregate> fiveHit = fiveHitFetchOfficer(officerId);

                            assertEquals(pairTwoHit.getSecond().getTotalResults(),
                                    pairTwoHitORSO.getSecond().getTotalResults());
                            assertEquals(pairTwoHit.getSecond().getResignedCount(),
                                    pairTwoHitORSO.getSecond().getResignedCount());
                            assertEquals(pairTwoHit.getSecond().getInactiveCount(),
                                    pairTwoHitORSO.getSecond().getInactiveCount());

                            assertEquals(pairTwoHit.getSecond().getTotalResults(),
                                    fourHitORSO.getSecond().getTotalResults());
                            assertEquals(pairTwoHit.getSecond().getResignedCount(),
                                    fourHitORSO.getSecond().getResignedCount());
                            assertEquals(pairTwoHit.getSecond().getInactiveCount(),
                                    fourHitORSO.getSecond().getInactiveCount());

                            assertEquals(pairTwoHit.getSecond().getTotalResults(),
                                    fiveHit.getSecond().getTotalResults());
                            assertEquals(pairTwoHit.getSecond().getResignedCount(),
                                    fiveHit.getSecond().getResignedCount());
                            assertEquals(pairTwoHit.getSecond().getInactiveCount(),
                                    fiveHit.getSecond().getInactiveCount());

                            return new Timings(officerId, pairTwoHit.getFirst(),
                                    pairTwoHitORSO.getFirst(), fourHitORSO.getFirst(), fiveHit.getFirst());

                        } catch (Exception e) {
                            System.out.printf("\nOfficer ID %s: Failed to complete", officerId);
                        }
                        return new Timings(officerId, 0L, 0L, 0L, 0L);
                    })
                    .collect(Collectors.toList());
        } finally {
//            officerIds.stream()
//                    .parallel()
//                    .forEach(officerId -> {
//                        Query query = new Query()
//                                .addCriteria(Criteria.where("officer_id").is(officerId));
//                        mongoTemplate.remove(query, DELTA_APPOINTMENTS_COLLECTION);
//                        assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
//                    });
        }

        timings.forEach((v) -> {
            System.out.printf(
                    "\nOfficer ID: %s,\n\tTwo hit: %d\n\tTwo hit officer role sort order: %d\n\tFour hit officer role sort order: %d, \n\tfive hit: %d",
                    v.officerId, v.twoHitFetch, v.twoHitFetchOfficerRoleSortOrder, v.fourHitFetchOfficerRoleSortOrder,
                    v.fiveHitFetch);
        });

        Double avgTwoHit = timings.stream()
                .mapToDouble(Timings::getTwoHitFetch)
                .average()
                .orElse(0);
        Double avgTwoHitFetchOfficerRoleSortOrder = timings.stream()
                .mapToDouble(Timings::getTwoHitFetchOfficerRoleSortOrder)
                .average()
                .orElse(0);
        Double avgFourHitFetchOfficerRoleSortOrder = timings.stream()
                .mapToDouble(Timings::getFourHitFetchOfficerRoleSortOrder)
                .average()
                .orElse(0);
        Double avgFiveHit = timings.stream()
                .mapToDouble(Timings::getFiveHitFetch)
                .average()
                .orElse(0);

        System.out.printf(
                "\nAverages:\n\tTwo hit: %f\n\tTwo hit officer role sort order: %f\n\tFour hit officer role sort order: %f\n\tFive hit: %f",
                avgTwoHit.doubleValue(), avgTwoHitFetchOfficerRoleSortOrder.doubleValue(),
                avgFourHitFetchOfficerRoleSortOrder.doubleValue(), avgFiveHit.doubleValue());
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

    private Pair<Long, OfficerAppointmentsAggregate> fiveHitFetchOfficer(String officerId) {
        Instant start = Instant.now();
        try {
            OfficerAppointmentsAggregate twoHit = service.findOfficerCorrectSortingSeparateCalls(officerId,
                    new Filter(false, List.of()), 0, 500);
            return Pair.of(Duration.between(start, Instant.now()).toMillis(), twoHit);
        } catch (Exception e) {
            System.out.println("fiveHit correct sort order failed");
            e.printStackTrace();
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

    private static void createOfficer(String officerId, int appointmentCount) {

        String rawJson;
        try {
            rawJson = IOUtils.resourceToString("/internal-appointment-data.json",
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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