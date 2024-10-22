package uk.gov.companieshouse.company_appointments.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompanyAppointmentFullRecordControllerITest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String NULL_FIELD_COMPANY_NUMBER = "CN008888";
    private static final String NULL_FIELD_APPOINTMENT_ID = "testNullFieldsId123";
    private static final String DELTA_AT = "20220925171003950844";
    private static final String X_DELTA_AT = "x-delta-at";

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5");

    @MockBean
    private ResourceChangedApiService resourceChangedApiService;

    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data-with-null-fields.json", StandardCharsets.UTF_8)),
                "delta_appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void test200AndOfficerIsPersistedWithNoEmptyFields() throws Exception {
        // This test not only tests a 200 OK response when a PUT request is sent, but, more importantly, it tests
        // the jackson object mapper is working correctly when converting a json request to a java model when the
        // json request contains an empty field (e.g. "locality": ""). We want this to be converted to null and so
        // not persisted to MongoDB.

        String requestBody =
                IOUtils.resourceToString("/PUT_full_record_request_body_with_empty_locality_fields.json",
                        StandardCharsets.UTF_8);

        ResultActions result = mockMvc
                .perform(put("/company/{company_number}/appointments/{appointment_id}/full_record", COMPANY_NUMBER,
                        APPOINTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-Identity", "123").header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "internal-app")
                        .header("x-request-id", "contextId")
                        .content(requestBody));

        result.andExpect(status().isOk());

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("testEmptyFieldExampleId1234"));

        List<CompanyAppointmentDocument> appointmentDocuments = mongoTemplate.find(query, CompanyAppointmentDocument.class);
        assertEquals(1, appointmentDocuments.size());

        CompanyAppointmentDocument document = appointmentDocuments.getFirst();
        assertNull(document.getSensitiveData().getUsualResidentialAddress().getLocality());
        assertNull(document.getData().getServiceAddress().getLocality());
    }

    @Test
    void test200AndOfficerIsReturnedWithNullFieldsRemoved() throws Exception {
        ResultActions result = mockMvc
                .perform(get("/company/{company_number}/appointments/{appointment_id}/full_record",
                        NULL_FIELD_COMPANY_NUMBER, NULL_FIELD_APPOINTMENT_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "sensitive-data"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("null"));
    }

    @Test
    void testReturn200IfOfficerIsDeleted() throws Exception{
        ResultActions result = mockMvc
                .perform(delete("/company/{company_number}/appointments/{appointment_id}/full_record/delete", COMPANY_NUMBER,
                        APPOINTMENT_ID)
                        .header("ERIC-Identity", "123").header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "internal-app")
                        .header("x-request-id", "contextId")
                        .header(X_DELTA_AT, DELTA_AT));

        result.andExpect(status().isOk());

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("7IjxamNGLlqtIingmTZJJ42Hw9Q"));
        List<CompanyAppointmentDocument> appointments = mongoTemplate.find(query, CompanyAppointmentDocument.class);
        assertThat(appointments).isEmpty();
    }

    @Test
    void testReturnFullRecordGetMapsFieldNamesCorrectly() throws Exception{
        mockMvc.perform(get("/company/{company_number}/appointments/{appointment_id}/full_record", COMPANY_NUMBER, APPOINTMENT_ID)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "sensitive-data"))
                .andExpect(jsonPath("$.service_address.address_line_1", notNullValue()))
                .andExpect(jsonPath("$.service_address.address_line_2", notNullValue()))
                .andExpect(jsonPath("$.service_address.postal_code", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.address_line_1", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.address_line_2", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.care_of", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.po_box", notNullValue()))
                .andExpect(jsonPath("$.usual_residential_address.postal_code", notNullValue()))
                .andExpect(jsonPath("$.identification.identification_type", notNullValue()))
                .andExpect(jsonPath("$.identification.legal_authority", notNullValue()))
                .andExpect(jsonPath("$.identification.legal_form", notNullValue()))
                .andExpect(jsonPath("$.identification.place_registered", notNullValue()))
                .andExpect(jsonPath("$.identification.registration_number", notNullValue()))
                .andExpect(jsonPath("$.contact_details.contact_name", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.address_line_1", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.address_line_2", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.care_of", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.po_box", notNullValue()))
                .andExpect(jsonPath("$.principal_office_address.postal_code", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldPublishResourceChangedWhenAppointmentHasAlreadyBeenDeleted(CapturedOutput capture) throws Exception{
        ResultActions result = mockMvc
                .perform(delete("/company/{company_number}/appointments/{appointment_id}/full_record/delete",
                        COMPANY_NUMBER, "Incorrect")
                        .header("ERIC-Identity", "123").header("ERIC-Identity-Type", "key")
                        .header("ERIC-authorised-key-privileges", "internal-app")
                        .header("x-request-id", "contextId")
                        .header(X_DELTA_AT, DELTA_AT));

        result.andExpect(status().isOk());

        verify(resourceChangedApiService).invokeChsKafkaApi(any());
        assertThat(capture.getOut()).doesNotContain("event: error");
    }
}
