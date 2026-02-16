package uk.gov.companieshouse.company_appointments.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.company_appointments.roles.DirectorRoles.DIRECTOR;
import static uk.gov.companieshouse.company_appointments.roles.SecretarialRoles.SECRETARY;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.roles.DirectorRoles;
import uk.gov.companieshouse.company_appointments.roles.LlpRoles;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyAppointmentRepositoryTest {

    private static final String APPOINTMENT_ID = "appointment";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String REGISTER_TYPE_DIRECTORS = "directors";
    private static final String REGISTER_TYPE_SECRETARIES = "secretaries";
    private static final String REGISTER_TYPE_LLP_MEMBERS = "llp_members";
    private static final LocalDateTime APPOINTED_ON_BASE = LocalDateTime.of(2020, 1, 1, 10, 0);
    private static final LocalDateTime RESIGNED_ON_BASE = LocalDateTime.of(2022, 1, 1, 10, 0);
    private static final String SURNAME = "Surname";
    private static final String COLLECTION = "delta_appointments";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.2.5");

    @Autowired
    private CompanyAppointmentRepository repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private Document templateDocument;

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    @BeforeEach
    void setup() throws IOException {
        mongoTemplate.dropCollection(COLLECTION);
        mongoTemplate.createCollection(COLLECTION);

        templateDocument = Document.parse(
                IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8));
    }

    @Test
    void shouldFindAllAppointmentsForCompany() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + 1, DIRECTOR.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + 1, DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 0, 5, false, false);

        // then
        assertEquals(2, result.size());
    }

    @Test
    void shouldFindAllAppointmentsAndReturnFirstPage() {
        // given
        for (int i = 0; i < 8; i++) {
            insertAppointment(APPOINTMENT_ID + i, SURNAME, DIRECTOR.getRole(),
                    APPOINTED_ON_BASE.plusDays(i), null);
        }

        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 0, 5, false, false);

        // then
        assertEquals(5, result.size());
    }

    @Test
    void shouldFindAllAppointmentsAndReturnSecondPage() {
        // given
        for (int i = 0; i < 8; i++) {
            insertAppointment(APPOINTMENT_ID + i, SURNAME, DIRECTOR.getRole(),
                    APPOINTED_ON_BASE.plusDays(i), null);
        }

        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 5, 5, false, false);

        // then
        assertEquals(3, result.size());
    }

    @Test
    void shouldFindAllAppointmentsForCompanyDefaultSorting() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 0, 5, false, false);

        // then
        assertEquals(4, result.size());
        // Rule: Secretary > Director, Appointed On DESC
        assertEquals(APPOINTMENT_ID + 2, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 1, result.get(1).getId());
        // Rule: Surname ASC
        assertEquals(APPOINTMENT_ID + 4, result.get(2).getId());
        assertEquals(APPOINTMENT_ID + 3, result.get(3).getId());
    }

    @Test
    void shouldFindAllAppointmentsForCompanyDefaultSortingWithResignedDirector() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), RESIGNED_ON_BASE);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 0, 5, false, false);

        // then
        assertEquals(4, result.size());
        // Rule: Secretary > Director, Appointed On DESC
        assertEquals(APPOINTMENT_ID + 2, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 1, result.get(1).getId());
        // Rule: Active > Resigned
        assertEquals(APPOINTMENT_ID + 3, result.get(2).getId());
        assertEquals(APPOINTMENT_ID + 4, result.get(3).getId());
    }

    @Test
    void shouldFindAllAppointmentsForCompanyDefaultSortingWithResignedDirectorAndSecretary() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), APPOINTED_ON_BASE.plusDays(1));
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), RESIGNED_ON_BASE);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 0, 5, false, false);

        // then
        assertEquals(4, result.size());
        // Rule: Secretary > Director, Appointed On DESC
        assertEquals(APPOINTMENT_ID + 1, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 3, result.get(1).getId());
        // Rule: Active > Resigned
        assertEquals(APPOINTMENT_ID + 2, result.get(2).getId());
        assertEquals(APPOINTMENT_ID + 4, result.get(3).getId());
    }

    @Test
    void shouldFindAllActiveAppointmentsForCompanyDefaultSortingWithResignedDirector() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), RESIGNED_ON_BASE);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 0, 5, false, true);

        // then
        assertEquals(3, result.size());
        assertEquals(APPOINTMENT_ID + 2, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 1, result.get(1).getId());
        assertEquals(APPOINTMENT_ID + 3, result.get(2).getId());
    }

    @ParameterizedTest
    @EnumSource(SecretarialRoles.class)
    void shouldFindAllAppointmentsWithRegisterViewTrueForSecretaries(SecretarialRoles role) {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", role.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", role.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), RESIGNED_ON_BASE);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, REGISTER_TYPE_SECRETARIES, 0, 5, true, true);

        // then
        assertEquals(2, result.size());
        assertEquals(APPOINTMENT_ID + 2, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 1, result.get(1).getId());
    }

    @ParameterizedTest
    @EnumSource(DirectorRoles.class)
    void shouldFindAllAppointmentsWithRegisterViewTrueForDirectors(DirectorRoles role) {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", role.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", role.getRole(),
                APPOINTED_ON_BASE.plusDays(1), RESIGNED_ON_BASE);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, REGISTER_TYPE_DIRECTORS, 0, 5, true, true);

        // then
        assertEquals(1, result.size());
        assertEquals(APPOINTMENT_ID + 3, result.getFirst().getId());
    }

    @ParameterizedTest
    @EnumSource(LlpRoles.class)
    void shouldFindAppointmentsWithRegisterViewTrueForLLPMembers(LlpRoles role) {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", role.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", role.getRole(),
                APPOINTED_ON_BASE.plusDays(1), RESIGNED_ON_BASE);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, REGISTER_TYPE_LLP_MEMBERS, 0, 5, true, true);

        // then
        assertEquals(1, result.size());
        // Surname > Appointed On
        assertEquals(APPOINTMENT_ID + 3, result.getFirst().getId());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithInvalidRegisterView() {
        // given
        // when
        Executable executable = () -> repository.getCompanyAppointments(COMPANY_NUMBER,
                null, "imposter", 0, 5, true, true);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Invalid registerType of imposter", exception.getMessage());
    }

    @Test
    void shouldFindAllAppointmentsForCompanySortedByAppointedOnDate() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + 1, DIRECTOR.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + 2, DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                SortMapper.APPOINTED_ON, null, 0, 5, false, false);

        // then
        assertEquals(2, result.size());
        assertEquals(APPOINTMENT_ID + 2, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 1, result.get(1).getId());
    }

    @Test
    void shouldFindAllAppointmentsForCompanySortedBySurname() {
        // given
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", DIRECTOR.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        // when
        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                SortMapper.SURNAME, null, 0, 5, false, false);

        // then
        assertEquals(2, result.size());
        assertEquals(APPOINTMENT_ID + 2, result.get(0).getId());
        assertEquals(APPOINTMENT_ID + 1, result.get(1).getId());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexIsLargerThanSizeOfListThrowsNotFoundException() {
        insertAppointment(APPOINTMENT_ID + 1, SURNAME + "B", SECRETARY.getRole(), APPOINTED_ON_BASE,
                null);
        insertAppointment(APPOINTMENT_ID + 2, SURNAME + "A", SECRETARY.getRole(),
                APPOINTED_ON_BASE.plusDays(2), null);
        insertAppointment(APPOINTMENT_ID + 3, SURNAME + "C", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);
        insertAppointment(APPOINTMENT_ID + 4, SURNAME + "A", DIRECTOR.getRole(),
                APPOINTED_ON_BASE.plusDays(1), null);

        List<CompanyAppointmentDocument> result = repository.getCompanyAppointments(COMPANY_NUMBER,
                null, null, 4, 5, false, false);

        assertTrue(result.isEmpty());
    }

    @SuppressWarnings({"unchecked"})
    private void insertAppointment(String appointmentId, String surname, String officerRole,
            LocalDateTime appointedOn, LocalDateTime resignedOn) {
        templateDocument.put("appointment_id", appointmentId);
        templateDocument.put("_id", appointmentId);

        Map<String, Object> officerData = (Map<String, Object>) templateDocument.get("data");
        officerData.put("officer_role", officerRole);
        switch (officerRole) {
            case "director":
            case "corporate-director":
            case "nominee-director":
            case "corporate-nominee-director":
                templateDocument.put("officer_role_sort_order", resignedOn == null ? 20 : 200);
                break;
            case "secretary":
            case "corporate-secretary":
            case "nominee-secretary":
            case "corporate-nominee-secretary":
                templateDocument.put("officer_role_sort_order", resignedOn == null ? 10 : 100);
                break;
            case "llp-member":
            case "corporate-llp-member":
                templateDocument.put("officer_role_sort_order", resignedOn == null ? 20 : 200);
                break;
            case "llp-designated-member":
            case "corporate-llp-designated-member":
                templateDocument.put("officer_role_sort_order", resignedOn == null ? 10 : 100);
                break;
            default:
                throw new IllegalArgumentException("Invalid role name");
        }
        officerData.put("surname", surname);
        officerData.put("appointed_on", appointedOn.toInstant(ZoneOffset.UTC));

        if (resignedOn != null) {
            officerData.put("resigned_on", resignedOn.toInstant(ZoneOffset.UTC));
        }

        mongoTemplate.insert(templateDocument, "delta_appointments");
    }
}
