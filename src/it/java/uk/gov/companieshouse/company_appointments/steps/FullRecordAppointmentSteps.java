package uk.gov.companieshouse.company_appointments.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.companieshouse.company_appointments.config.CucumberContext.CONTEXT;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

public class FullRecordAppointmentSteps {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "7IjxamNGLlqtIingmTZJJ42Hw9Q";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    private static final String IDENTITY_TYPE_KEY = "key";
    private static final String INTERNAL_APP_PRIVILEGES = "internal-app";
    private static final String DELTA_AT = "20220925171003950844";
    private static final String OFFICER_ID = "officer_id";
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String X_DELTA_AT = "x-delta-at";
    private static final String X_OFFICER_ID = "x-officer-id";

    private final HttpHeaders headers = new HttpHeaders();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private CompanyAppointmentRepository companyAppointmentRepository;

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

    @Given("the user is authenticated but not authorised")
    public void userIsAuthenticatedButNotAuthorised() {
        headers.set(ERIC_IDENTITY, "TEST_IDENTITY");
        headers.set(ERIC_IDENTITY_TYPE, IDENTITY_TYPE_KEY);
        headers.set(ERIC_AUTHORISED_KEY_PRIVILEGES, "");
    }


    @Given("the record does not already exist in the database")
    public void noRecordsInDatabase() {
        companyAppointmentRepository.deleteAll();
    }

    @Given("the delta for payload {string} is the most recent delta for {string}")
    public void thisDeltaIsNotStale(String dataFile, String appointmentId) throws IOException {
        CompanyAppointmentDocument actual = companyAppointmentRepository.findById(appointmentId)
                .orElseThrow(IllegalArgumentException :: new);

        File file = new ClassPathResource("input/" + dataFile + ".json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
        OffsetDateTime deltaAt = actual.getDeltaAt().atOffset(ZoneOffset.UTC);
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(deltaAt.plusDays(1L));

        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);
    }

    @Given("the delta for payload {string} is a stale delta for {string}")
    public void thisDeltaIsStale(String payloadFile, String appointmentId) throws IOException {
        CompanyAppointmentDocument actual = companyAppointmentRepository.findById(appointmentId)
                .orElseThrow(IllegalArgumentException::new);

        File file = new ClassPathResource("input/" + payloadFile + ".json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
        OffsetDateTime deltaAt = actual.getDeltaAt().atOffset(ZoneOffset.UTC);
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(deltaAt.minusDays(1L));

        CONTEXT.set("getRecord", fullRecordCompanyOfficerApi);
    }

    @Given("the record is not present in the delta_appointment database")
    public void recordNotFoundInDeltaAppointmentDatabase() {
        companyAppointmentRepository.deleteAll();
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
        headers.set(X_DELTA_AT, DELTA_AT);
        headers.set(X_OFFICER_ID, OFFICER_ID);

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        String uri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER, APPOINTMENT_ID);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class);

        CONTEXT.set("statusCode", response.getStatusCode());
    }

    @Then("the record should be saved")
    public void hasRecordSaved() {
        CompanyAppointmentDocument appointment = companyAppointmentRepository.findById(APPOINTMENT_ID)
                .orElseThrow(IllegalArgumentException:: new);
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");

        Instant requestDeltaAt = fullRecordCompanyOfficerApi.getInternalData().getDeltaAt().toInstant();
        Instant databaseDeltaAt = appointment.getDeltaAt();

        assertEquals(databaseDeltaAt, requestDeltaAt);
    }

    @Then("the changes within the delta for {string} should NOT be persisted in the database")
    public void deltaUpdateNotSavedInDatabase(String appointmentId) {
        CompanyAppointmentDocument appointment = companyAppointmentRepository.findById(appointmentId)
                .orElseThrow(IllegalArgumentException::new);
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = CONTEXT.get("getRecord");

        Instant requestDeltaAt = fullRecordCompanyOfficerApi.getInternalData().getDeltaAt().toInstant();
        Instant databaseDeltaAt = appointment.getDeltaAt();

        assertNotEquals(databaseDeltaAt, requestDeltaAt);
    }

    @Then("the record should NOT be deleted")
    public void recordShouldNotBeDeleted() {
        CompanyAppointmentDocument appointment = companyAppointmentRepository.findById(APPOINTMENT_ID)
                .orElseThrow(IllegalArgumentException:: new);
        assertThat(appointment).isNotNull();
    }

    @Then("the record should be deleted successfully")
    public void recordShouldBeDeletedSuccessFully() {
        Optional<CompanyAppointmentDocument> appointment = companyAppointmentRepository.findById(APPOINTMENT_ID);
        assertThat(appointment).isEmpty();
    }
}
