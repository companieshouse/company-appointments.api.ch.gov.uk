package uk.gov.companieshouse.company_appointments.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

public class CommonSteps {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String HASHED_APPOINTMENT_ID = "EcEKO1YhIKexb0cSDZsn_OHsFw4";

    private static final MongoTemplate mongoTemplate =
            new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));


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

    @Then("a request should NOT be sent to the resource changed endpoint")
    public void noRequestsSentToResourceChangedEndpoint() {
        verify(lessThanOrExactly(0), postRequestedFor(urlEqualTo("/resource-changed")));
    }

    @Then("the record should be saved")
    public void hasRecordSaved() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(HASHED_APPOINTMENT_ID));

        List<CompanyAppointmentDocument> appointments = mongoTemplate.find(query, CompanyAppointmentDocument.class);

        assertThat(appointments).hasSize(1);
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
