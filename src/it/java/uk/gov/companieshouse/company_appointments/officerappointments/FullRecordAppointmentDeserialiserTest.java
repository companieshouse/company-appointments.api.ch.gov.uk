package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;

@Testcontainers
@SpringBootTest(classes = CompanyAppointmentsApplication.class, webEnvironment = WebEnvironment.NONE)
class FullRecordAppointmentDeserialiserTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void shouldSerialiseJsonToFullRecordAppointment() throws Exception {
        File file = new ClassPathResource("fullRecordAppointmentsExamplePut.json").getFile();
        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = objectMapper.readValue(file, FullRecordCompanyOfficerApi.class);
        assertThat(fullRecordCompanyOfficerApi).isNotNull();
    }
}
