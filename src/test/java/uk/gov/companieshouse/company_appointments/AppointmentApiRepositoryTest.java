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
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AppointmentApiRepositoryTest {
    @Spy
    private CompanyAppointmentFullRecordRepository testRepository;

    @Captor
    private ArgumentCaptor<AppointmentApiEntity> captor;

    private final static Instant CREATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");
    private final static Instant UPDATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");

    @BeforeEach
    void setUp() {
    }

    @Test
    void insertOrUpdate() {
        final AppointmentAPI appointment = new AppointmentAPI("id",
                new OfficerAPI(),
                new SensitiveOfficerAPI(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "companyNumber",
                new InstantAPI(CREATED_AT),
                new InstantAPI(UPDATED_AT),
                "deltaAt",
                22);
        final AppointmentApiEntity expected = new AppointmentApiEntity(appointment);
        testRepository.insertOrUpdate(appointment);

        verify(testRepository).save(captor.capture());
        assertThat(captor.getValue(), is(equalTo(expected)));
    }
}
