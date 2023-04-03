package uk.gov.companieshouse.company_appointments.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.hu.De;
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
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.company_appointments.config.CucumberContext;
import uk.gov.companieshouse.company_appointments.config.WiremockTestConfig;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.util.DeltaDateValidator;
import uk.gov.companieshouse.company_appointments.util.FileReader;
import uk.gov.companieshouse.company_appointments.util.JSONMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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
//        mongoTemplate.insert(Document.parse(
//                        IOUtils.resourceToString("/natural_officer_full_record_PUT.json", StandardCharsets.UTF_8)),
//                "delta_appointments");
//        mongoTemplate.insert(Document.parse(
//                        IOUtils.resourceToString("/corporate_officer_full_record_PUT.json", StandardCharsets.UTF_8)),
//                "delta_appointments");

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
//        //essentially want to stub the boolean isDeltaStale method in the CompanyAppointmentFullRecordService here. To return false.
//        Optional<DeltaAppointmentApiEntity> existingAppointment = Optional.ofNullable(mongoTemplate.findById(HASHED_APPOINTMENT_ID, DeltaAppointmentApiEntity.class));
////        existingAppointment.get().setDeltaAt();
//        //can we somehow be clever with the cucumber context and get the appointment delta_at to be set the same as the existing appointment delta_at
//        // more importantly can we set these delta_at's to be different when we want delta to be stale?
//        File file = new ClassPathResource("src/it/resources/" + dataFile + ".json").getFile();
//        DeltaAppointmentApiEntity newAppointment = objectMapper.readValue(file, DeltaAppointmentApiEntity.class);
//        existingAppointment.get().setDeltaAt("");
//        newAppointment.setDeltaAt("");

//        Query query = new Query();
//        query.addCriteria(Criteria.where("_id").is(appointmentId));
//
//        List<DeltaAppointmentApiEntity> appointments = mongoTemplate.find(query, DeltaAppointmentApiEntity.class);
//
//        for(DeltaAppointmentApiEntity appointment : appointments){
//            CONTEXT.set("appointmentDeltaAt", appointment.getDeltaAt());
//        }

            File file = new ClassPathResource("input/" + dataFile + ".json").getFile();
            FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);

            fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(OffsetDateTime.parse("2021-04-23T18:25:43.511Z"));
            String recordData = fullRecordCompanyOfficerApi.toString();

            CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);

            String data = FileReader.readInputFile(dataFile);
            data.replaceAll("1.411661403E9", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

            CONTEXT.set("deltaData", data);
    }
/*
problem is when we are mapping it with the object mapper we are adding on a "class FullResourceCompanyOfficerApi"
at the start of the data in the string where there shouldn't be one.
 Hence 400 bad request when we send it to the controller because it is not in the format of FullRecordCompanyOfficerApi

 In the production code as well - we don't need the DeltaDateValidator anymore either as we Aren't needing to test that in that way
 Additionally, Maybe the simplest way of doing what i want to do with this step definition is to have multiple files with different delta_at's?
 Might be easier than messing around with cucumber CONTEXT and objects and an easy way to see if that is the reason for the 400 error.

 */

    @Given("the company appointments api is running")
    public void theApplicationRunning() {
        assertThat(restTemplate).isNotNull();
        assertThat(mongoDBContainer.isRunning()).isTrue();
    }

    @When("I send a PUT request with payload {string}")
    public void sendPutRequest(String payloadFile) throws IOException {
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");
//
        String data = fullRecordCompanyOfficerApi.toString();
        String data2 = CONTEXT.get("deltaData");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        headers.set("ERIC-Authorised-Key-Privileges", "internal-app");

        HttpEntity<String> request = new HttpEntity<>(data2, headers);
        String uri = "/company/" + COMPANY_NUMBER + "/appointments/" + HASHED_APPOINTMENT_ID + "/full_record";
        String uri2 = "/company/01777777/appointments/EcEKO1YhIKexb0cSDZsn_OHsFw4/full_record";

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

    private FullRecordCompanyOfficerApi buildFullRecordOfficer() {
        FullRecordCompanyOfficerApi output  = new FullRecordCompanyOfficerApi();

        ExternalData externalData = new ExternalData();
        Data data = new Data();
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());
        externalData.setData(data);
        externalData.setSensitiveData(sensitiveData);
        externalData.setAppointmentId("id");
        externalData.setCompanyNumber("companyNumber");
        externalData.setInternalId("internalId");
        externalData.setOfficerId("officerId");
        externalData.setPreviousOfficerId("previousOfficerId");
        InternalData internalData = new InternalData();
        internalData.setOfficerRoleSortOrder(22);
        internalData.setDeltaAt(CONTEXT.get("appointmentDeltaAt"));
        internalData.setUpdatedBy("updatedBy");
        output.setExternalData(externalData);
        output.setInternalData(internalData);
        return output;
    }
}
