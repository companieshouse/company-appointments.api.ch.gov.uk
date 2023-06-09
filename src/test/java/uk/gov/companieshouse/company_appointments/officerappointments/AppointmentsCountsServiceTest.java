package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentsCountsServiceTest {

    public static final String OFFICER_ID = "officerId";
    @InjectMocks
    private AppointmentsCountsService service;

    @Mock
    private OfficerAppointmentsRepository repository;

    @Test
    @DisplayName("Successfully gets counts when the filter is not enabled")
    void getAppointmentsCounts() {
        // given
        AppointmentsCounts expected = new AppointmentsCounts()
                .activeCount(3)
                .inactiveCount(2)
                .resignedCount(1);

        when(repository.countOfficerAppointments(anyString())).thenReturn(expected);

        // when
        AppointmentsCounts actual = service.getAppointmentsCounts(OFFICER_ID, false, 6);

        // then
        assertEquals(expected, actual);
        verify(repository).countOfficerAppointments(OFFICER_ID);
    }

    @Test
    @DisplayName("Successfully gets counts when the filter is not enabled")
    void getAppointmentsCountsFilterEnabled() {
        // given
        AppointmentsCounts expected = new AppointmentsCounts()
                .activeCount(3)
                .inactiveCount(0)
                .resignedCount(0);

        // when
        AppointmentsCounts actual = service.getAppointmentsCounts(OFFICER_ID, true, 3);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(repository);
    }
}