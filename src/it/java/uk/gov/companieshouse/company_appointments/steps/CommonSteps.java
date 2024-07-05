package uk.gov.companieshouse.company_appointments.steps;

import static com.github.tomakehurst.wiremock.client.WireMock.lessThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company_appointments.config.AbstractMongoConfig.mongoDBContainer;
import static uk.gov.companieshouse.company_appointments.config.CucumberContext.CONTEXT;
import static uk.gov.companieshouse.company_appointments.config.WiremockTestConfig.getServeEvents;
import static uk.gov.companieshouse.company_appointments.config.WiremockTestConfig.setupWiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.company_appointments.config.WiremockTestConfig;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.logging.Logger;

public class CommonSteps {

    @Autowired
    private ObjectMapper objectMapper;

    private static MongoTemplate mongoTemplate;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private CompanyAppointmentRepository companyAppointmentRepository;

    @Mock
    private Logger logger;

    @BeforeAll
    public static void setup() throws IOException {
        mongoDBContainer.start();
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.dropCollection("delta_appointments");
        mongoTemplate.createCollection("delta_appointments");
    }

    @Before
    public void beforeEachTest() throws IOException {
        CONTEXT.clear();
        setupWiremock();
        companyAppointmentRepository.deleteAll();
        mongoTemplate.insert(Document.parse(
                        IOUtils.resourceToString("/delta-appointment-data.json", StandardCharsets.UTF_8)),
                "delta_appointments");
    }

    @Given("CHS kafka is available")
    public void theChsKafkaApiIsAvailable() throws InterruptedException {
        WiremockTestConfig.stubKafkaApi(HttpStatus.OK.value());
    }

    @Given("CHS kafka is unavailable")
    public void theChsKafkaApiIsUnavailable() throws InterruptedException {
        WiremockTestConfig.stubKafkaApi(HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @Given("the company appointments api is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        assertThat(mongoDBContainer.isRunning()).isTrue();
    }

    @Then("I should receive a {int} status code")
    public void receiveStatusCode(int code) {
        HttpStatus statusCode = CONTEXT.get("statusCode");
        assertThat(statusCode).isEqualTo(HttpStatus.valueOf(code));
    }

    @Then("a request is sent to the resource changed endpoint")
    public void CHSKafkaInvokedSuccessFully() {
        verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/private/resource-changed")));
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

    @Then("a request should NOT be sent to the resource changed endpoint")
    public void noRequestsSentToResourceChangedEndpoint() throws InterruptedException {
        Thread.sleep(2000);
        verify(lessThanOrExactly(0), postRequestedFor(urlEqualTo("/private/resource-changed")));
    }

    private ChangedResource getPayloadFromWiremock() {
        ServeEvent serverEvent = getServeEvents().getFirst();
        String body = new String (serverEvent.getRequest().getBody());
        ChangedResource payload = null;
        try {
            payload = objectMapper.readValue(body, ChangedResource.class);
        } catch (JsonProcessingException e) {
            logger.error("error getting payload from wiremock in getPayloadFromWiremock()");
        }
        return payload;
    }
}
