package uk.gov.companieshouse.company_appointments.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;

import java.io.File;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SerializerITest {

    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @MockBean
    private ResourceChangedApiService resourceChangedApiService;

    @Autowired
    private CompanyAppointmentFullRecordRepository companyAppointmentFullRecordRepository;

    private static MongoTemplate mongoTemplate;

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void testThatDatesAndDateTimeAreSerialisedAndDeSerialisedCorrectly() throws Exception {
        File file = new ClassPathResource("fullRecordAppointmentsExamplePut.json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);

        mockMvc.perform(put("/company/{company_number}/appointments/{appointment_id}/full_record",
                        fullRecordCompanyOfficerApi.getExternalData().getCompanyNumber(),
                        fullRecordCompanyOfficerApi.getExternalData().getAppointmentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(X_REQUEST_ID, "5342342")
                        .header(ERIC_IDENTITY, "SOME_IDENTITY")
                        .header(ERIC_IDENTITY_TYPE, "key")
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "internal-app")
                        .content(objectMapper.writeValueAsString(fullRecordCompanyOfficerApi)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        CompanyAppointmentDocument appointment = companyAppointmentFullRecordRepository.findById(APPOINTMENT_ID)
                .orElseThrow(IllegalArgumentException:: new);

        assertThat(appointment.getData().getAppointedOn()).isEqualTo("2011-11-03");
        assertThat(appointment.getData().getAppointedOn()).isNotInstanceOf(LocalDateTime.class);
        assertThat(appointment.getDeltaAt()).isEqualTo("2021-04-30T18:25:43.511Z");
    }
}
