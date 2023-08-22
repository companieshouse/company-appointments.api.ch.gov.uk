package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.company_appointments.interceptor.AuthenticationHelperImpl.ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OfficerAppointmentsControllerITest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");
    private static final String DELTA_APPOINTMENTS_COLLECTION = "delta_appointments";
    private static MongoTemplate mongoTemplate;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection(DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data3.json", StandardCharsets.UTF_8)), DELTA_APPOINTMENTS_COLLECTION);
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
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.name", is("Noname1 Noname2 NOSURNAME")))
                .andExpect(jsonPath("$.start_index", is(0)))
                .andExpect(jsonPath("$.total_results", is(2)));
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
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId, itemsPerPage)
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
        mongoTemplate.findAllAndRemove(query, DELTA_APPOINTMENTS_COLLECTION);

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
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId, itemsPerPage)
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
        mongoTemplate.findAllAndRemove(query, DELTA_APPOINTMENTS_COLLECTION);

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
                    .replaceAll("<id>", UUID.randomUUID().toString())
                    .replaceAll("<officerId>", officerId));
            documentsToInsert.add(document);
        }
        mongoTemplate.insert(documentsToInsert, DELTA_APPOINTMENTS_COLLECTION);

        //when
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId, requestedItemsPerPage)
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
        mongoTemplate.findAllAndRemove(query, DELTA_APPOINTMENTS_COLLECTION);

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
        ResultActions result = mockMvc.perform(get("/officers/{officer_id}/appointments?items_per_page={itemsPerPage}", officerId, requestedItemsPerPage)
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
}
