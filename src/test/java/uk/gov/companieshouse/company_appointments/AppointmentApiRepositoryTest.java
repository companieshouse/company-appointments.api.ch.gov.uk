package uk.gov.companieshouse.company_appointments;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaTimestamp;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AppointmentApiRepositoryTest {
    @Spy
    private CompanyAppointmentFullRecordRepository testRepository;

    @Captor
    private ArgumentCaptor<CompanyAppointmentDocument> captor;

    private final static Instant CREATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");
    private final static Instant UPDATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");

    @BeforeEach
    void setUp() {
    }

    @Test
    void insertOrUpdate() {
        final CompanyAppointmentDocument document = CompanyAppointmentDocument.Builder.builder()
                .withId("id")
                .withData(new DeltaOfficerData())
                .withSensitiveData(new DeltaSensitiveData())
                .withInternalId("internalId")
                .withAppointmentId("appointmentId")
                .withOfficerId("officerId")
                .withPreviousOfficerId("previousOfficerId")
                .withCompanyNumber("companyNumber")
                .withUpdated(new DeltaTimestamp(UPDATED_AT))
                .withUpdatedBy("updatedBy")
                .withCreated(new DeltaTimestamp(CREATED_AT))
                .withDeltaAt("deltaAt")
                .withOfficerRoleSortOrder(22)
                .withCompanyName("company name")
                .withCompanyStatus("company status")
                .build();
        testRepository.insertOrUpdate(document);

        verify(testRepository).save(captor.capture());
        assertThat(captor.getValue(), is(equalTo(document)));
    }
}
