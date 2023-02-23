package uk.gov.companieshouse.company_appointments.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import uk.gov.companieshouse.company_appointments.config.CucumberContext;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.util.FileReader;
import uk.gov.companieshouse.company_appointments.util.JSONMatcher;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.company_appointments.config.AbstractMongoConfig.mongoDBContainer;

public class CommonSteps {

    private static String COMPANY_NUMBER = "12345678";
    private static String HASHED_APPOINTMENT_ID = "EcEKO1YhIKexb0cSDZsn_OHsFw4";

    private static MongoTemplate mongoTemplate =
            new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private JSONMatcher jsonMatcher;

    @BeforeAll
    static void dbSetUp(){
        mongoTemplate.createCollection("delta_appointments");
    }

    @Given("the company appointments api is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        assertThat(mongoDBContainer.isRunning()).isTrue();
    }

    @When("I send a PUT request with payload {string}")
    public void sendPutRequest(String payloadFile) {
        String data = FileReader.readInputFile(payloadFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Privileges", "internal-app");

        HttpEntity<String> request = new HttpEntity<>(data, headers);
        String uri = "/company/" + COMPANY_NUMBER + "/appointments/" + HASHED_APPOINTMENT_ID + "/full_record";

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode());
    }

    @When("I send a GET request with id {string}")
    public void sendGetRequest(String id) throws JsonProcessingException {
        String data = FileReader.readDataFile(id);
        Document document = Document.parse(data);
        mongoTemplate.insert(Document.parse(data), "delta_appointments");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        DeltaAppointmentApiEntity entity = mapper.readValue(document.toJson(), DeltaAppointmentApiEntity.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Privileges", "sensitive-data");

        HttpEntity<String> request = new HttpEntity<>(data, headers);
        String uri = "/company/" + COMPANY_NUMBER + "/appointments/" + id + "/full_record";

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.GET, request, Void.class);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCode());
        CucumberContext.CONTEXT.set("responseData", response.getBody());
    }

    @Then("I should receive a {int} status code")
    public void receiveStatusCode(int code) {
        HttpStatus statusCode = CucumberContext.CONTEXT.get("statusCode");
        assertThat(statusCode).isEqualTo(HttpStatus.valueOf(code));
    }

    @Then("the record should be saved")
    public void hasRecordSaved() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(HASHED_APPOINTMENT_ID));

        List<DeltaAppointmentApiEntity> appointments = mongoTemplate.find(query, DeltaAppointmentApiEntity.class);

        assertThat(appointments.size()).isEqualTo(1);
    }

    @Then("the result should match {string}")
    public void resultShouldMatchData(String name) {
        String expected = FileReader.readOutputFile(name);

        String actual = CucumberContext.CONTEXT.get("responseData");

        jsonMatcher.doJSONsMatch(expected, actual);
    }
}
