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
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;

@ExtendWith(MockitoExtension.class)
class AppointmentApiRepositoryTest {
    @Spy
    private AppointmentApiRepository testRepository;

    @Captor
    private ArgumentCaptor<AppointmentApiEntity> captor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void insertOrUpdate() {
        final AppointmentAPI appointment = new AppointmentAPI("id",
                new OfficerAPI(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "companyNumber",
                "deltaAt");
        final AppointmentApiEntity expected = new AppointmentApiEntity(appointment);
        testRepository.insertOrUpdate(appointment);

        verify(testRepository).save(captor.capture());
        assertThat(captor.getValue(), is(equalTo(expected)));
    }
}
