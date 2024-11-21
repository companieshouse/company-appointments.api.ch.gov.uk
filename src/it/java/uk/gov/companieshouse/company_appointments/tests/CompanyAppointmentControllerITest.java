package uk.gov.companieshouse.company_appointments.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static uk.gov.companieshouse.company_appointments.util.TestUtils.COMPANY_STATUSES;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CompanyAppointmentControllerITest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    private static final String CONTEXT_ID = "context_id";
    private static Map<String, String> companyStatusToCompanyNumber;

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyMetricsApiService companyMetricsApiService;

    @Mock
    private MetricsApi metricsApi;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(IOUtils.resourceToString("/appointment-data2.json", StandardCharsets.UTF_8)), "delta_appointments");
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");

        createCompanyNumbers();
        mongoTemplate.insert(buildCorporateAppointments(), "delta_appointments");
        mongoTemplate.insert(buildNaturalAppointments(), "delta_appointments");
    }

    @Test
    void testReturn200OKIfOfficerIsFound() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, "active_1")
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointed_on", is("2024-08-26")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date_of_birth", not(contains("day"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date_of_birth.year", is(1980)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date_of_birth.month", is(1)));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void testReturn404IfOfficerIsNotFound(CapturedOutput capture) throws Exception {
        // when
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER, "missing")
                        .header(X_REQUEST_ID, CONTEXT_ID)
                        .header(ERIC_IDENTITY, "123")
                        .header(ERIC_IDENTITY_TYPE, "oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound());
        assertThat(capture.getOut()).doesNotContain("event: error");
    }

    @Test
    void testReturn401IfUserNotAuthenticated() throws Exception {
        // when
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/appointments/{appointment_id}", COMPANY_NUMBER,
                        APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testReturn200OKIfAllOfficersAreFound() throws Exception {
        //when
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(3)
                .activeCount(2)
                .resignedCount(1)));
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers", COMPANY_NUMBER)
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("Doe, John Forename")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("GLOVER, PHIL Peter")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[2].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(3)));
    }

    @Test
    void shouldReturnZeroCountsWhenOfficerForCompanyIsNotFoundAndMetricsPresent() throws Exception {
        // when
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/officers", "87654321")
                        .header(X_REQUEST_ID, CONTEXT_ID)
                        .header(ERIC_IDENTITY, "123")
                        .header(ERIC_IDENTITY_TYPE, "oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items", is(new ArrayList<>())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.inactive_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(0)))
                .andExpect((MockMvcResultMatchers.jsonPath("$.kind", is("officer-list"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start_index", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items_per_page", is(35)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.etag", is("")));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldReturnZeroCountsWhenOfficerForCompanyIsNotFoundAndNoMetricsAreFound(CapturedOutput capture) throws Exception {
        // when
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(404, null, metricsApi));
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/officers", "87654321")
                        .header(X_REQUEST_ID, CONTEXT_ID)
                        .header(ERIC_IDENTITY, "123")
                        .header(ERIC_IDENTITY_TYPE, "oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items", is(new ArrayList<>())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.inactive_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(0)))
                .andExpect((MockMvcResultMatchers.jsonPath("$.kind", is("officer-list"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start_index", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items_per_page", is(35)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.etag", is("")));
        assertThat(capture.getOut()).doesNotContain("event: error");
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldReturnNotFoundWhenOfficerForCompanyIsFoundButNoMetricsAreFound(CapturedOutput capture) throws Exception {
        // when
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(404, null, metricsApi));

        // then
        mockMvc.perform(get("/company/{company_number}/officers", COMPANY_NUMBER)
                        .header(X_REQUEST_ID, CONTEXT_ID)
                        .header(ERIC_IDENTITY, "123")
                        .header(ERIC_IDENTITY_TYPE, "oauth2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
        assertThat(capture.getOut()).doesNotContain("event: error");
    }

    @Test
    void testReturn200OKIfAllOfficersAreFoundWithFilter() throws Exception {
        //when
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(2)
                .activeCount(2)
                .resignedCount(0)));
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers?filter=active", COMPANY_NUMBER)
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("GLOVER, PHIL Peter")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(2)));
    }

    @Test
    void testReturn200OkWithOfficersOrderedByAppointedOn() throws Exception {
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(2)
                .activeCount(2)
                .resignedCount(0)));
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers?order_by=appointed_on", COMPANY_NUMBER)
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("Doe, John Forename")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(2)));
    }

    @Test
    void testReturn200OkWithOfficersOrderedBySurname() throws Exception {
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(3)
                .activeCount(2)
                .resignedCount(1)));
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers?order_by=surname", COMPANY_NUMBER)
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[0].name", is("Doe, John Forename")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[1].name", is("GLOVER, PHIL Peter")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.[2].name", is("NOSURNAME, Noname1 Noname2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active_count", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resigned_count", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_results", is(3)));
    }

    @Test
    void testReturn400BadRequestWithIncorrectOrderBy() throws Exception {
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi()
                .appointments(new AppointmentsApi()));
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers?order_by=invalid", COMPANY_NUMBER)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Returns 200 ok when PATCH existing appointments request handled successfully")
    void testPatchExistingAppointmentCompanyNameStatus() throws Exception {
        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("active");

        mockMvc.perform(patch("/company/{company_number}/appointments", COMPANY_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION,
                        String.format("/company/%s/officers", COMPANY_NUMBER)));
    }

    @Test
    @DisplayName("Patch existing appointments endpoint returns 400 bad request when company name is missing")
    void testPatchExistingAppointmentCompanyNameStatusMissingRequestFields() throws Exception {
        mockMvc.perform(patch("/company/{company_number}/appointments", COMPANY_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(new PatchAppointmentNameStatusApi())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Patch existing appointments endpoint returns 400 when invalid company status provided")
    void testPatchExistingAppointmentCompanyNameStatusInvalidStatus() throws Exception {
        PatchAppointmentNameStatusApi requestBody = new PatchAppointmentNameStatusApi()
                .companyName("company name")
                .companyStatus("fake");

        mockMvc.perform(patch("/company/{company_number}/appointments", COMPANY_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Patch existing appointments endpoint returns 403 forbidden when internal app privileges is missing")
    void testPatchExistingAppointmentCompanyNameStatusAppPrivilegesMissing() throws Exception {
        mockMvc.perform(patch("/company/{company_number}/appointments", COMPANY_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "invalid"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
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
        "removed" })
    void testReturn200OKWithActiveCompaniesWithActiveFilter(String companyStatus) throws Exception {
        // given
        final int totalNumberOfAppointmentsForEachCompany = 189;
        final int numberOfAppointmentsConsideredActiveForEachCompany = 126;
        final int resignedCount = totalNumberOfAppointmentsForEachCompany - numberOfAppointmentsConsideredActiveForEachCompany;

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(totalNumberOfAppointmentsForEachCompany)
                .activeCount(numberOfAppointmentsConsideredActiveForEachCompany)
                .resignedCount(resignedCount)));

        // when
        final String companyNumber = companyStatusToCompanyNumber.get(companyStatus);
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers?filter=active&items_per_page=500", companyNumber)
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final OfficerList responseEntity = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), OfficerList.class);

        assertEquals(numberOfAppointmentsConsideredActiveForEachCompany, responseEntity.getTotalResults());
        assertEquals(numberOfAppointmentsConsideredActiveForEachCompany, responseEntity.getActiveCount());
        assertEquals(resignedCount, responseEntity.getResignedCount());
        assertEquals(numberOfAppointmentsConsideredActiveForEachCompany, responseEntity.getItems().size());
    }

    @ParameterizedTest
    @CsvSource({
        "dissolved",
        "converted-closed",
        "closed" })
    void testReturn200OKWithInactiveCompaniesWithActiveFilter(String companyStatus) throws Exception {
        // given
        final int totalNumberOfAppointmentsForEachCompany = 189;
        final int numberOfAppointmentsConsideredActiveForEachCompany = 126;
        final int resignedCount = totalNumberOfAppointmentsForEachCompany - numberOfAppointmentsConsideredActiveForEachCompany;

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(totalNumberOfAppointmentsForEachCompany)
                .activeCount(numberOfAppointmentsConsideredActiveForEachCompany)
                .resignedCount(resignedCount)));

        // when
        final String companyNumber = companyStatusToCompanyNumber.get(companyStatus);
        ResultActions result = mockMvc.perform(get("/company/{company_number}/officers?filter=active", companyNumber)
                .header(X_REQUEST_ID, CONTEXT_ID)
                .header(ERIC_IDENTITY, "123")
                .header(ERIC_IDENTITY_TYPE, "key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final OfficerList responseEntity = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), OfficerList.class);

        assertEquals(0, responseEntity.getTotalResults());
        assertEquals(0, responseEntity.getActiveCount());
        assertEquals(0, responseEntity.getResignedCount());
        assertEquals(0, responseEntity.getItems().size());
    }

    /**
     * Generates 12 company numbers, one for each type of company status, and creates a Map of
     * status (key) : company number (value).
     */
    private static void createCompanyNumbers() {
        companyStatusToCompanyNumber = new HashMap<>();
        for (final String status : COMPANY_STATUSES) {
            final String companyNumber = generateRandomEightCharCompanyNumber();
            companyStatusToCompanyNumber.put(status, companyNumber);
        }
    }

    private static List<Document> buildCorporateAppointments() throws IOException {
        List<String> corporateOfficerRoles = new ArrayList<>();
        for (final String role : OFFICER_ROLES) {
            if (role.contains("corporate")) { // exclude non-corporate roles
                corporateOfficerRoles.add(role);
            }
        }

        List<Document> documents = new ArrayList<>();
        for (final String path :CORPORATE_APPOINTMENT_DOC_PATHS) { // loops 3 times
            final String json = IOUtils.resourceToString(path, StandardCharsets.UTF_8);

            for (final String type : IDENTITY_TYPES) { // loops 5 times

                for (final String role : corporateOfficerRoles) { // loops 10 times
                    Document appointmentDocument;
                    String nextJson = json
                            .replaceAll("<identification_type>", type)
                            .replaceAll("<officer_role>", role);

                    for (Map.Entry<String, String> entry : companyStatusToCompanyNumber.entrySet()) { // loops 12 times
                        appointmentDocument = Document.parse(nextJson
                                .replaceAll("<company_status>", entry.getKey())
                                .replaceAll("<internal_id>", generateRandomInternalId())
                                .replaceAll("<id>", UUID.randomUUID().toString())
                                .replaceAll("<company_number>", entry.getValue())
                                .replaceAll("<officer_id>", UUID.randomUUID().toString()));

                        documents.add(appointmentDocument);
                    }
                }
            }
        }
        return documents; // results in 1800 documents
    }

    private static List<Document> buildNaturalAppointments() throws IOException {
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
                Document appointmentDocument;
                String nextJson = json
                        .replaceAll("<officer_role>", role);

                for (Map.Entry<String, String> entry : companyStatusToCompanyNumber.entrySet()) { // loops 12 times
                    appointmentDocument = Document.parse(nextJson
                            .replaceAll("<company_status>", entry.getKey())
                            .replaceAll("<internal_id>", generateRandomInternalId())
                            .replaceAll("<id>", UUID.randomUUID().toString())
                            .replaceAll("<company_number>", entry.getValue())
                            .replaceAll("<officer_id>", UUID.randomUUID().toString()));

                    documents.add(appointmentDocument);
                }
            }
        }
        return documents; // results in 468 documents
    }
}
