package uk.gov.companieshouse.company_appointments;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaTimestamp;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentApiRepositoryTest {
    @Spy
    private CompanyAppointmentRepository testRepository;

    @Captor
    private ArgumentCaptor<CompanyAppointmentDocument> captor;

    private final static Instant CREATED_AT = Instant.parse("2021-08-01T00:00:00.000000Z");
    private final static Instant UPDATED_AT = Instant.parse("2021-08-01T00:00:00.000000Z");

    @BeforeEach
    void setUp() {
    }

    @Test
    void insertOrUpdate() {
        final CompanyAppointmentDocument document = new CompanyAppointmentDocument()
                .id("id")
                .data(new DeltaOfficerData())
                .sensitiveData(new DeltaSensitiveData())
                .internalId("internalId")
                .appointmentId("appointmentId")
                .officerId("officerId")
                .previousOfficerId("previousOfficerId")
                .companyNumber("companyNumber")
                .updated(new DeltaTimestamp(UPDATED_AT))
                .updatedBy("updatedBy")
                .created(new DeltaTimestamp(CREATED_AT))
                .deltaAt(Instant.parse("2022-01-12T00:00:00.000000Z"))
                .officerRoleSortOrder(22)
                .companyName("company name")
                .companyStatus("company status");
        testRepository.insertOrUpdate(document);

        verify(testRepository).save(captor.capture());
        assertThat(captor.getValue(), is(equalTo(document)));
    }
}
