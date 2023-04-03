package uk.gov.companieshouse.company_appointments.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.config.WiremockTestConfig;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.util.DeltaDateValidator;
import uk.gov.companieshouse.company_appointments.util.JSONMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company_appointments.config.AbstractMongoConfig.mongoDBContainer;
import static uk.gov.companieshouse.company_appointments.config.CucumberContext.CONTEXT;

public class CommonSteps {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String HASHED_APPOINTMENT_ID = "EcEKO1YhIKexb0cSDZsn_OHsFw4";

    private static MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private JSONMatcher jsonMatcher;

    @Autowired
    private CompanyAppointmentFullRecordRepository companyAppointmentFullRecordRepository;

    @Autowired
    private DeltaDateValidator deltaDateValidator;

    @BeforeAll
    public static void setup() throws IOException {
        mongoDBContainer.start();
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");

        WiremockTestConfig.setupWiremock();

    }

    @Given("the record does not already exist in the database")
    public void noRecordsInDatabase() {
        companyAppointmentFullRecordRepository.deleteAll();
    }

    @Given("CHS kafka is available")
    public void theChsKafkaApiIsAvailable() {
        WiremockTestConfig.stubKafkaApi(HttpStatus.OK.value());
    }

    @Given("the delta for payload {string} is the most recent delta for {string} and {string}")
    public void thisDeltaIsNotStale(String dataFile, String companyNumber, String appointmentId) throws IOException {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("7IjxamNGLlqtIingmTZJJ42Hw9Q"));
        DeltaAppointmentApiEntity appointment = mongoTemplate.find(query, DeltaAppointmentApiEntity.class)
                .get(0);

        File file = new ClassPathResource("input/" + dataFile + ".json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
//        OffsetDateTime deltaAt = OffsetDateTime.parse(appointment.getDeltaAt());
//        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(deltaAt.plusDays(1L));

        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);
    }

//    @Given("the delta for payload {string} is stale")
//    public void thisDeltaIsStale(String dataFile) throws IOException {
//        File file = new ClassPathResource("input/" + dataFile + ".json").getFile();
//        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
//        OffsetDateTime deltaAt = fullRecordCompanyOfficerApi.getInternalData().getDeltaAt();
//        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(deltaAt.plusDays(1L));
//
//        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);
//    }


    @Given("the company appointments api is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        assertThat(mongoDBContainer.isRunning()).isTrue();
    }

    @When("I send a PUT request with payload {string}")
    public void sendPutRequest(String payloadFile) throws IOException {
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");
        String json = objectMapper.writeValueAsString(fullRecordCompanyOfficerApi);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Privileges", "internal-app");

        HttpEntity<String> request = new HttpEntity<>(json, headers);
        String uri = "/company/" + COMPANY_NUMBER + "/appointments/" + HASHED_APPOINTMENT_ID + "/full_record";


        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class);

        CONTEXT.set("statusCode", response.getStatusCode());
    }

    @Then("I should receive a {int} status code")
    public void receiveStatusCode(int code) {
        HttpStatus statusCode = CONTEXT.get("statusCode");
        assertThat(statusCode).isEqualTo(HttpStatus.valueOf(code));
    }

    @Then("the record should be saved")
    public void hasRecordSaved() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(HASHED_APPOINTMENT_ID));

        List<DeltaAppointmentApiEntity> appointments = mongoTemplate.find(query, DeltaAppointmentApiEntity.class);

        assertThat(appointments).hasSize(1);
    }

    @Then("CHS kafka is invoked successfully")
    public void CHSKafkaInvokedSuccessFully() {
        verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/resource-changed")));
    }
}
