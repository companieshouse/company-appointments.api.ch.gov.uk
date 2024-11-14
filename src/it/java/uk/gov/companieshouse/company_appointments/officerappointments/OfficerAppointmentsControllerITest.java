package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.company_appointments.interceptor.AuthenticationHelperImpl.ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.CORPORATE_APPOINTMENT_DOC_PATHS;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.IDENTITY_TYPES;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.NATURAL_APPOINTMENT_DOC_PATHS;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.OFFICER_ROLES;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.generateRandomEightCharCompanyNumber;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.generateRandomInternalId;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OfficerAppointmentsControllerITest {

    private static final String CORPORATE_OFFICER_ID = UUID.randomUUID().toString();
    private static final String NATURAL_OFFICER_ID = UUID.randomUUID().toString();
    private static final String NON_ACTIVE_OFFICER_ID = "non_active_officer_ID";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6");
    private static final String DELTA_APPOINTMENTS_COLLECTION = "delta_appointments";
    private static MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection(DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)),
                DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)),
                DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(
                Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)),
                DELTA_APPOINTMENTS_COLLECTION);
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @DisplayName("Should return HTTP 200 OK and a list of appointments for a particular officer id")
    @Test
    void getOfficerAppointments() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "5VEOBB4a9dlB_iugw_vieHjWpCk")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(35)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is("/officers/5VEOBB4a9dlB_iugw_vieHjWpCk/appointments")))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(3)))
                .andExpect(jsonPath("$.active_count", is(2)))
                .andExpect(jsonPath("$.resigned_count", is(1)))
                .andExpect(jsonPath("$.inactive_count", is(0)));
    }

    @DisplayName("Should return HTTP 404 Not Found when no appointments exist for a particular officer id")
    @Test
    void getOfficerAppointmentsNotFound() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "officerId")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "oauth2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("Should return HTTP 400 Bad Request when an invalid filter parameter is supplied")
    @Test
    void getOfficerAppointmentsBadRequest() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "5VEOBB4a9dlB_iugw_vieHjWpCk")
                .param("filter", "invalid")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "oauth2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest());
    }

    @DisplayName("Should return HTTP 401 Unauthorised if the request has no auth headers")
    @Test
    void getOfficerAppointmentsUnauthenticated() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments", "5VEOBB4a9dlB_iugw_vieHjWpCk")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @DisplayName("Should return HTTP 200 OK and a list of 500 appointments for a particular officer id when using internal app privileges")
    @Test
    void getOfficerAppointmentsInternal() throws Exception {
        // given
        final String officerId = UUID.randomUUID().toString();
        final int itemsPerPage = 500;

        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < itemsPerPage; i++) {
            String rawJson = IOUtils.resourceToString("/internal-appointment-data.json", StandardCharsets.UTF_8);
            Document document = Document.parse(rawJson
                    .replaceAll("<id>", UUID.randomUUID().toString())
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);

        //when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId, itemsPerPage)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER, "internal-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(itemsPerPage)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is(String.format("/officers/%s/appointments", officerId))))
                .andExpect(jsonPath("$.items", hasSize(itemsPerPage)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(itemsPerPage)));

        // clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(officerId));
        mongoTemplate.remove(query, DELTA_APPOINTMENTS_COLLECTION);

        assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
    }

    @DisplayName("Should return HTTP 200 OK and a list of 100 appointments for a particular officer id when using internal app privileges and items per page is set to 500")
    @Test
    void getOfficer100AppointmentsInternal() throws Exception {
        // given
        final String officerId = UUID.randomUUID().toString();
        final int itemsPerPage = 500;
        final int appointmentsCount = 100;

        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < appointmentsCount; i++) {
            String rawJson = IOUtils.resourceToString("/internal-appointment-data.json", StandardCharsets.UTF_8);
            Document document = Document.parse(rawJson
                    .replaceAll("<id>", UUID.randomUUID().toString())
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);

        //when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId, itemsPerPage)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER, "internal-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(itemsPerPage)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is(String.format("/officers/%s/appointments", officerId))))
                .andExpect(jsonPath("$.items", hasSize(appointmentsCount)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(appointmentsCount)));

        // clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(officerId));
        mongoTemplate.remove(query, DELTA_APPOINTMENTS_COLLECTION);

        assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
    }

    @DisplayName("Should return HTTP 200 OK and a list of 500 appointments for a particular officer id when using internal app privileges and items per page is above 500")
    @Test
    void getOfficerAppointmentsInternalWhenItemsPerPageExceeds500() throws Exception {
        // given
        final String officerId = UUID.randomUUID().toString();
        final int expectedItemsPerPage = 500;
        final int requestedItemsPerPage = 550;

        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < requestedItemsPerPage; i++) {
            String rawJson = IOUtils.resourceToString("/internal-appointment-data.json", StandardCharsets.UTF_8);
            Document document = Document.parse(rawJson
                    .replaceAll("<id>", ("id-" + i))
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);

        //when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId,
                        requestedItemsPerPage)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER, "internal-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(expectedItemsPerPage)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is(String.format("/officers/%s/appointments", officerId))))
                .andExpect(jsonPath("$.items", hasSize(expectedItemsPerPage)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(requestedItemsPerPage)));

        // clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(officerId));
        mongoTemplate.remove(query, DELTA_APPOINTMENTS_COLLECTION);

        assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
    }

    @DisplayName("Should return HTTP 200 OK and a list of 500K appointments for an officer with 400K appointments")
    @Test
    @Disabled("Causes a Java Heap memory exception on Concourse")
    void getOfficerAppointmentsInternalWhenOfficerHas150KAppointments() throws Exception {
        // given
        final String officerId = UUID.randomUUID().toString();
        final int expectedItemsPerPage = 500;
        final int requestedItemsPerPage = 500;
        final int appointmentCount = 400_000;

        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < appointmentCount; i++) {
            String rawJson = IOUtils.resourceToString("/internal-appointment-data.json", StandardCharsets.UTF_8);
            Document document = Document.parse(rawJson
                    .replaceAll("<id>", UUID.randomUUID().toString())
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);

        //when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId,
                        requestedItemsPerPage)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("X-Request-Id", "requestId")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER, "internal-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(expectedItemsPerPage)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is(String.format("/officers/%s/appointments", officerId))))
                .andExpect(jsonPath("$.items", hasSize(expectedItemsPerPage)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(appointmentCount)));

        // clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(officerId));
        mongoTemplate.remove(query, DELTA_APPOINTMENTS_COLLECTION);

        assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
    }

    @DisplayName("Should return HTTP 200 OK and a list of 50 appointments for a particular officer id when not using internal app privileges and items per page is above 500")
    @Test
    void getOfficerAppointmentsExternalWhenItemsPerPageExceeds500() throws Exception {
        // given
        final String officerId = UUID.randomUUID().toString();
        final int expectedItemsPerPage = 50;
        final int requestedItemsPerPage = 550;

        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < requestedItemsPerPage; i++) {
            String rawJson = IOUtils.resourceToString("/internal-appointment-data.json", StandardCharsets.UTF_8);
            Document document = Document.parse(rawJson
                    .replaceAll("<id>", UUID.randomUUID().toString())
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);

        //when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId,
                        requestedItemsPerPage)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(expectedItemsPerPage)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self", is(String.format("/officers/%s/appointments", officerId))))
                .andExpect(jsonPath("$.items", hasSize(expectedItemsPerPage)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(requestedItemsPerPage)));

        // clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(officerId));
        mongoTemplate.findAllAndRemove(query, DELTA_APPOINTMENTS_COLLECTION);

        assertTrue(mongoTemplate.find(query, CompanyAppointmentDocument.class).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "active",
            "liquidation",
            "receivership",
            "voluntary-arrangement",
            "insolvency-proceedings",
            "administration",
            "open",
            "registered",
            "removed"})
    void getCorporateOfficerAppointmentsToCompaniesConsideredActiveWithActiveFilterApplied(String companyStatus)
            throws Exception {
        // given
        final int defaultItemsPerPage = 35;
        final int numberOfCorpAppointmentsConsideredActive = 100;
        mongoTemplate.insert(buildCorporateAppointments(companyStatus), "delta_appointments");

        // when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?filter=active", CORPORATE_OFFICER_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final AppointmentList responseEntity = objectMapper.readValue(
                result.andReturn().getResponse().getContentAsString(), AppointmentList.class);

        assertEquals(defaultItemsPerPage, responseEntity.getItems().size());
        assertEquals(defaultItemsPerPage, responseEntity.getItemsPerPage());
        assertEquals(numberOfCorpAppointmentsConsideredActive, responseEntity.getTotalResults());
        assertEquals(numberOfCorpAppointmentsConsideredActive, responseEntity.getActiveCount());
        assertEquals(0, responseEntity.getResignedCount());
        assertEquals(0, responseEntity.getInactiveCount());

        for (int i = 0; i < defaultItemsPerPage; i++) {
            assertNull(responseEntity.getItems().get(i).getResignedOn());
        }

        // Clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(CORPORATE_OFFICER_ID));
        mongoTemplate.findAllAndRemove(query, "delta_appointments");
    }

    @ParameterizedTest
    @CsvSource({
            "active",
            "liquidation",
            "receivership",
            "voluntary-arrangement",
            "insolvency-proceedings",
            "administration",
            "open",
            "registered",
            "removed"})
    void getNaturalOfficerAppointmentsToCompaniesConsideredActiveWithActiveFilterApplied(String companyStatus)
            throws Exception {
        // given
        final int defaultItemsPerPage = 35;
        final int numberOfNaturalAppointmentsConsideredActive = 26;
        mongoTemplate.insert(buildNaturalAppointments(companyStatus), "delta_appointments");

        // when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?filter=active", NATURAL_OFFICER_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final AppointmentList responseEntity = objectMapper.readValue(
                result.andReturn().getResponse().getContentAsString(), AppointmentList.class);

        assertEquals(defaultItemsPerPage, responseEntity.getItemsPerPage());
        assertEquals(numberOfNaturalAppointmentsConsideredActive, responseEntity.getTotalResults());
        assertEquals(numberOfNaturalAppointmentsConsideredActive, responseEntity.getActiveCount());
        assertEquals(0, responseEntity.getResignedCount());
        assertEquals(0, responseEntity.getInactiveCount());

        for (int i = 0; i < numberOfNaturalAppointmentsConsideredActive; i++) {
            assertNull(responseEntity.getItems().get(i).getResignedOn());
        }

        // Clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(NATURAL_OFFICER_ID));
        mongoTemplate.findAllAndRemove(query, "delta_appointments");
    }

    @ParameterizedTest
    @CsvSource({
            "dissolved",
            "converted-closed",
            "closed"})
    void getCorporateOfficerAppointmentsToCompaniesConsideredInactiveWithActiveFilterApplied(String companyStatus)
            throws Exception {
        // given
        mongoTemplate.insert(buildCorporateAppointments(companyStatus), "delta_appointments");

        // when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?filter=active", CORPORATE_OFFICER_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final AppointmentList responseEntity = objectMapper.readValue(
                result.andReturn().getResponse().getContentAsString(), AppointmentList.class);

        assertEquals(0, responseEntity.getItems().size());
        assertEquals(35, responseEntity.getItemsPerPage());
        assertEquals(0, responseEntity.getTotalResults());
        assertEquals(0, responseEntity.getActiveCount());
        assertEquals(0, responseEntity.getResignedCount());
        assertEquals(0, responseEntity.getInactiveCount());
        assertTrue(responseEntity.getItems().isEmpty());

        // Clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(CORPORATE_OFFICER_ID));
        mongoTemplate.findAllAndRemove(query, "delta_appointments");
    }

    @ParameterizedTest
    @CsvSource({
            "dissolved",
            "converted-closed",
            "closed"})
    void getNaturalOfficerAppointmentsToCompaniesConsideredInactiveWithActiveFilterApplied(String companyStatus)
            throws Exception {
        // given
        mongoTemplate.insert(buildNaturalAppointments(companyStatus), "delta_appointments");

        // when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments?filter=active", NATURAL_OFFICER_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final AppointmentList responseEntity = objectMapper.readValue(
                result.andReturn().getResponse().getContentAsString(), AppointmentList.class);

        assertEquals(35, responseEntity.getItemsPerPage());
        assertEquals(0, responseEntity.getTotalResults());
        assertEquals(0, responseEntity.getActiveCount());
        assertEquals(0, responseEntity.getResignedCount());
        assertEquals(0, responseEntity.getInactiveCount());
        assertTrue(responseEntity.getItems().isEmpty());

        // Clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(NATURAL_OFFICER_ID));
        mongoTemplate.findAllAndRemove(query, "delta_appointments");
    }

    @DisplayName("Should use the name of the most recently appointed appointment when there are no active appointments")
    @Test
    void getNonActiveOfficerAppointments() throws Exception {
        // given
        mongoTemplate.insert(IOUtils.resourceToString("/appointmentdocuments/resigned_officer_appointment.json",
                StandardCharsets.UTF_8), "delta_appointments");
        mongoTemplate.insert(IOUtils.resourceToString("/appointmentdocuments/inactive_officer_appointment.json",
                StandardCharsets.UTF_8), "delta_appointments");

        // when
        ResultActions result = mockMvc.perform(
                get("/officers/{officer_id}/appointments", NON_ACTIVE_OFFICER_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.total_results", is(2)))
                .andExpect(jsonPath("$.active_count", is(0)))
                .andExpect(jsonPath("$.resigned_count", is(1)))
                .andExpect(jsonPath("$.inactive_count", is(1)))
                .andExpect(jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(jsonPath("$.date_of_birth.year", is(1970)))
                .andExpect(jsonPath("$.date_of_birth.month", is(1)))
                .andExpect(jsonPath("$.is_corporate_officer", is(false)))
                .andExpect(jsonPath("$.items_per_page", is(35)))
                .andExpect(jsonPath("$.kind", is("personal-appointment")))
                .andExpect(jsonPath("$.links.self",
                        is("/officers/%s/appointments".formatted(NON_ACTIVE_OFFICER_ID))))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)));

        // Clean up
        Query query = new Query()
                .addCriteria(Criteria.where("officer_id").is(NON_ACTIVE_OFFICER_ID));
        mongoTemplate.findAllAndRemove(query, "delta_appointments");
    }

    private static List<Document> buildCorporateAppointments(String companyStatus) throws IOException {
        List<String> corporateOfficerRoles = new ArrayList<>();
        for (final String role : OFFICER_ROLES) {
            if (role.contains("corporate")) { // exclude non-corporate roles
                corporateOfficerRoles.add(role);
            }
        }

        List<Document> documents = new ArrayList<>();
        for (final String path : CORPORATE_APPOINTMENT_DOC_PATHS) { // loops 3 times
            final String json = IOUtils.resourceToString(path, StandardCharsets.UTF_8);

            for (final String type : IDENTITY_TYPES) { // loops 5 times

                for (final String role : corporateOfficerRoles) { // loops 10 times
                    Document appointmentDocument = Document.parse(json
                            .replaceAll("<identification_type>", type)
                            .replaceAll("<officer_role>", role)
                            .replaceAll("<company_status>", companyStatus)
                            .replaceAll("<internal_id>", generateRandomInternalId())
                            .replaceAll("<id>", UUID.randomUUID().toString())
                            .replaceAll("<company_number>", generateRandomEightCharCompanyNumber())
                            .replaceAll("<officer_id>", CORPORATE_OFFICER_ID));

                    documents.add(appointmentDocument);
                }
            }
        }
        return documents; // results in 150 documents
    }

    private static List<Document> buildNaturalAppointments(String companyStatus) throws IOException {
        List<String> corporateOfficerRoles = new ArrayList<>();
        for (final String role : OFFICER_ROLES) {
            if (!role.contains("corporate")) { // exclude corporate roles
                corporateOfficerRoles.add(role);
            }
        }

        List<Document> documents = new ArrayList<>();
        for (final String path : NATURAL_APPOINTMENT_DOC_PATHS) { // loops 3 times
            final String json = IOUtils.resourceToString(path, StandardCharsets.UTF_8);

            for (final String role : corporateOfficerRoles) { // loops 13 times
                Document appointmentDocument = Document.parse(json
                        .replaceAll("<officer_role>", role)
                        .replaceAll("<company_status>", companyStatus)
                        .replaceAll("<internal_id>", generateRandomInternalId())
                        .replaceAll("<id>", UUID.randomUUID().toString())
                        .replaceAll("<company_number>", generateRandomEightCharCompanyNumber())
                        .replaceAll("<officer_id>", NATURAL_OFFICER_ID));

                documents.add(appointmentDocument);
            }
        }
        return documents; // results in 39 documents
    }
}
