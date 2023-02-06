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
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AppointmentApiRepositoryTest {
    @Spy
    private CompanyAppointmentFullRecordRepository testRepository;

    @Captor
    private ArgumentCaptor<DeltaAppointmentApiEntity> captor;

    private final static Instant CREATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");
    private final static Instant UPDATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");

    @BeforeEach
    void setUp() {
    }

    @Test
    void insertOrUpdate() {
        final DeltaAppointmentApi appointment = new DeltaAppointmentApi("id", "etag",
                new Data(),
                new SensitiveData(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "companyNumber",
                new InstantAPI(UPDATED_AT),
                "updateBy",
                new InstantAPI(CREATED_AT),
                "deltaAt",
                22);
        final DeltaAppointmentApiEntity expected = new DeltaAppointmentApiEntity(appointment);
        testRepository.insertOrUpdate(appointment);

        verify(testRepository).save(captor.capture());
        assertThat(captor.getValue(), is(equalTo(expected)));
    }
}
