package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.api.model.officerappointments.OfficerAppointmentsApi;

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
    private OfficerAppointmentsApi officerAppointmentsApi;

    @Mock
    private OfficerAppointmentsAggregate officerAppointmentsAggregate;

    @Mock
    private AppointmentApi appointmentApi;

    @Test
    @DisplayName("Get officer appointments returns an officer appointments api")
    void getOfficerAppointments() {
        // given
        OfficerAppointmentsRequest request = new OfficerAppointmentsRequest(OFFICER_ID, null, null, null);
        when(repository.findOfficerAppointments(anyString())).thenReturn(Optional.of(officerAppointmentsAggregate));
        when(mapper.mapOfficerAppointments(any(), any())).thenReturn(Optional.of(officerAppointmentsApi));

        // when
        Optional<OfficerAppointmentsApi> actual = service.getOfficerAppointments(request);

        // then
        assertTrue(actual.isPresent());
        assertEquals(officerAppointmentsApi, actual.get());
        verify(repository).findOfficerAppointments(OFFICER_ID);
        verify(mapper).mapOfficerAppointments(officerAppointmentsAggregate, request);
    }

    @Test
    @DisplayName("Get officer appointments returns empty")
    void getOfficerAppointmentsEmpty() {
        // given
        OfficerAppointmentsRequest request = new OfficerAppointmentsRequest(OFFICER_ID, null, null, null);
        when(repository.findOfficerAppointments(anyString())).thenReturn(Optional.empty());

        // when
        Optional<OfficerAppointmentsApi> actual = service.getOfficerAppointments(request);

        // then
        assertFalse(actual.isPresent());
        verify(repository).findOfficerAppointments(OFFICER_ID);
        verifyNoInteractions(mapper);
    }
}