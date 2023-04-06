package uk.gov.companieshouse.company_appointments.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.compress.utils.CharsetNames;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.company_appointments.config.WiremockTestConfig;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.company_appointments.config.AbstractMongoConfig.mongoDBContainer;
import static uk.gov.companieshouse.company_appointments.config.CucumberContext.CONTEXT;
import static uk.gov.companieshouse.company_appointments.config.WiremockTestConfig.getServeEvents;
import static uk.gov.companieshouse.company_appointments.config.WiremockTestConfig.setupWiremock;

public class CommonSteps {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static MongoTemplate mongoTemplate;
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    private static final String IDENTITY_TYPE_KEY = "key";
    private static final String INTERNAL_APP_PRIVILEGES = "internal-app";

    private static final String X_REQUEST_ID = "x-request-id";

    private final HttpHeaders headers = new HttpHeaders();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private CompanyAppointmentFullRecordRepository companyAppointmentFullRecordRepository;

    @BeforeAll
    public static void setup() throws IOException {
        mongoDBContainer.start();
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection("delta_appointments");
    }

    @Before
    public void beforeEachTest() throws IOException {
        CONTEXT.clear();
        setupWiremock();
        companyAppointmentFullRecordRepository.deleteAll();
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");
    }

    @Given("CHS kafka is available")
    public void theChsKafkaApiIsAvailable() {
        WiremockTestConfig.stubKafkaApi(HttpStatus.OK.value());
    }

    @Given("CHS kafka is unavailable")
    public void theChsKafkaApiIsUnavailable() {
        WiremockTestConfig.stubKafkaApi(HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @Given("the record does not already exist in the database")
    public void noRecordsInDatabase() {
        companyAppointmentFullRecordRepository.deleteAll();
    }

    @Given("the delta for payload {string} is the most recent delta for {string}")
    public void thisDeltaIsNotStale(String dataFile, String appointmentId) throws IOException {
        DeltaAppointmentApiEntity actual = companyAppointmentFullRecordRepository.findById(appointmentId)
                .orElseThrow(IllegalArgumentException :: new);

        File file = new ClassPathResource("input/" + dataFile + ".json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
        OffsetDateTime deltaAt = OffsetDateTime.parse(actual.getDeltaAt());
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(deltaAt.plusDays(1L));

        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);
    }

    @Given("the delta for payload {string} is a stale delta for {string}")
    public void thisDeltaIsStale(String payloadFile, String appointmentId) throws IOException {
        DeltaAppointmentApiEntity actual = companyAppointmentFullRecordRepository.findById(appointmentId)
                .orElseThrow(IllegalArgumentException::new);

        File file = new ClassPathResource("input/" + payloadFile + ".json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
        OffsetDateTime deltaAt = OffsetDateTime.parse(actual.getDeltaAt());
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(deltaAt.minusDays(1L));

        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);
    }

    @Given("the company appointments api is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        assertThat(mongoDBContainer.isRunning()).isTrue();
    }

    @Given("the user is authenticated and authorised with internal app privileges")
    public void userIsAuthenticatedAndAuthorisedWithInternalAppPrivileges() {
        headers.set(ERIC_IDENTITY, "TEST_IDENTITY");
        headers.set(ERIC_IDENTITY_TYPE, IDENTITY_TYPE_KEY);
        headers.set(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_APP_PRIVILEGES);
    }

    @Given("the user is not authenticated or authorised")
    public void userIsNotAuthenticatedOrAuthorised() {
        headers.set(ERIC_IDENTITY, "");
        headers.set(ERIC_IDENTITY_TYPE, "");
        headers.set(ERIC_AUTHORISED_KEY_PRIVILEGES, "");
    }

    @When("a request is sent to the PUT endpoint to upsert an officers delta")
    public void sendPutRequestWithoutFilename() throws IOException {
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");
        String json = objectMapper.writeValueAsString(fullRecordCompanyOfficerApi);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set(X_REQUEST_ID, "5234234234");

        HttpEntity<String> request = new HttpEntity<>(json, headers);
        String uri = String.format("/company/%s/appointments/%s/full_record",
                fullRecordCompanyOfficerApi.getExternalData().getCompanyNumber(),
                fullRecordCompanyOfficerApi.getExternalData().getAppointmentId());

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class);

        CONTEXT.set("statusCode", response.getStatusCode());
    }

    @When("I send a PUT request with payload {string}")
    public void sendPutRequestWithFilename(String payloadFile) throws IOException {
        File file = new ClassPathResource("input/" + payloadFile + ".json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);

        String json = objectMapper.writeValueAsString(fullRecordCompanyOfficerApi);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set(X_REQUEST_ID, "5234234234");

        HttpEntity<String> request = new HttpEntity<>(json, headers);
        String uri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class);

        CONTEXT.set("statusCode", response.getStatusCode());
    }

    @When("a request is sent to the DELETE endpoint to delete an officer")
    public void sendDeleteRequest() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set(X_REQUEST_ID, "5234234234");

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        String uri = String.format("/company/%s/appointments/%s/full_record/delete", COMPANY_NUMBER,
                APPOINTMENT_ID);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class);

        CONTEXT.set("statusCode", response.getStatusCode());
    }

    @Then("I should receive a {int} status code")
    public void receiveStatusCode(int code) {
        HttpStatus statusCode = CONTEXT.get("statusCode");
        assertThat(statusCode).isEqualTo(HttpStatus.valueOf(code));
    }

    @Then("the record should be saved")
    public void hasRecordSaved() {
        DeltaAppointmentApiEntity appointment = companyAppointmentFullRecordRepository.findById(APPOINTMENT_ID)
                .orElseThrow(IllegalArgumentException:: new);
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");
        String requestDeltaAt = String.valueOf(fullRecordCompanyOfficerApi.getInternalData().getDeltaAt());

        String databaseDeltaAt = appointment.getDeltaAt();
        ServiceAddress databaseAddress = appointment.getData().getServiceAddress();
        ServiceAddress requestAddress = fullRecordCompanyOfficerApi.getExternalData().getData().getServiceAddress();

        assertEquals(databaseAddress, requestAddress);
        assertEquals(databaseDeltaAt, requestDeltaAt);

    }

    @Then("the changes within the delta for {string} should NOT be persisted in the database")
    public void deltaUpdateNotSavedInDatabase(String appointmentId) {
        DeltaAppointmentApiEntity appointment = companyAppointmentFullRecordRepository.findById(appointmentId)
                .orElseThrow(IllegalArgumentException::new);
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");

        String requestDeltaAt = String.valueOf(fullRecordCompanyOfficerApi.getInternalData().getDeltaAt());
        String databaseDeltaAt = appointment.getDeltaAt();
        ServiceAddress databaseAddress = appointment.getData().getServiceAddress();
        ServiceAddress requestAddress = fullRecordCompanyOfficerApi.getExternalData().getData().getServiceAddress();

        assertNotEquals(databaseAddress, requestAddress);
        assertNotEquals(databaseDeltaAt, requestDeltaAt);
    }

    @Then("the record should NOT be deleted")
    public void recordShouldNotBeDeleted() {
        DeltaAppointmentApiEntity appointment = companyAppointmentFullRecordRepository.findById(APPOINTMENT_ID)
                .orElseThrow(IllegalArgumentException:: new);
        assertThat(appointment).isNotNull();
    }

    @Then("the record should be deleted successfully")
    public void recordShouldBeDeletedSuccessFully() {
        Optional<DeltaAppointmentApiEntity> appointment = companyAppointmentFullRecordRepository.findById(APPOINTMENT_ID);
        assertThat(appointment).isEmpty();
    }


    @Then("a request is sent to the resource changed endpoint")
    public void CHSKafkaInvokedSuccessFully() {
        verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/resource-changed")));
    }

    @Then("the event type is {string}")
    public void eventTypeIs(String eventType) {
        ChangedResource payload = getPayloadFromWiremock();
        assertThat(payload).isNotNull();
        assertThat(payload.getEvent().getType()).isEqualTo(eventType);
    }

    @Then("the request body is a valid resource changed request")
    public void requestBodySentToResourceChangedIsValidPut() {
        ChangedResource payload = getPayloadFromWiremock();
        assertThat(payload).isInstanceOf(ChangedResource.class);
        assertThat(payload.getEvent().getType()).isEqualTo("changed");
    }

    @Then("the request body is a valid resource deleted request")
    public void requestBodySentToResourceChangedISValidDelete() {
        ChangedResource payload = getPayloadFromWiremock();
        assertThat(payload).isInstanceOf(ChangedResource.class);
        assertThat(payload.getEvent().getType()).isEqualTo("deleted");
    }

    @Given("the record is not present in the database")
    public void recordNotFoundInDatabase() {
        companyAppointmentFullRecordRepository.deleteAll();
    }

    @Then("a request should NOT be sent to the resource changed endpoint")
    public void noRequestsSentToResourceChangedEndpoint() {
        verify(lessThanOrExactly(0), postRequestedFor(urlEqualTo("/resource-changed")));
    }

    private ChangedResource getPayloadFromWiremock() {
        ServeEvent serverEvent = getServeEvents().get(0);
        String body = new String (serverEvent.getRequest().getBody());
        ChangedResource payload = null;
        try {
            payload = objectMapper.readValue(body, ChangedResource.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return payload;
    }
}
