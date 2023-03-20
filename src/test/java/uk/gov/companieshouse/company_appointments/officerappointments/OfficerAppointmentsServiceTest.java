package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.api.officer.AppointmentList;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsServiceTest {

    private static final String OFFICER_ID = "officerId";

    @InjectMocks
    private OfficerAppointmentsService service;

    @Mock
    private OfficerAppointmentsRepository repository;

    @Mock
    private OfficerAppointmentsMapper mapper;

    @Mock
    private AppointmentList officerAppointments;

    @Mock
    private OfficerAppointmentsAggregate officerAppointmentsAggregate;

    @Mock
    private AppointmentApi appointmentApi;

    @Test
    @DisplayName("Get officer appointments returns an officer appointments api")
    void getOfficerAppointments() {
        // given
        OfficerAppointmentsRequest request = new OfficerAppointmentsRequest(OFFICER_ID, "", null, null);
        when(repository.findOfficerAppointments(anyString(), anyBoolean())).thenReturn(officerAppointmentsAggregate);
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(officerAppointments));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(request);

        // then
        assertTrue(actual.isPresent());
        assertEquals(officerAppointments, actual.get());
        verify(repository).findOfficerAppointments(OFFICER_ID, true);
        verify(mapper).mapOfficerAppointments(officerAppointmentsAggregate);
    }
}