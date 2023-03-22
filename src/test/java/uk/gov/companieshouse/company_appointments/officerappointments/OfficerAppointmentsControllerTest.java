package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsControllerTest {

    private final static String OFFICER_ID = "567890";

    @InjectMocks
    private OfficerAppointmentsController controller;

    @Mock
    private OfficerAppointmentsService service;

    @Mock
    private AppointmentList officerAppointments;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<String> loggerCaptor;

    @Test
    @DisplayName("Call to get officer appointments returns http 200 ok and officer appointments api")
    void testGetOfficerAppointments() {
        // given
        OfficerAppointmentsRequest request = new OfficerAppointmentsRequest(OFFICER_ID, null, null, null);
        when(service.getOfficerAppointments(any())).thenReturn(Optional.of(officerAppointments));

        // when
        ResponseEntity<AppointmentList> response = controller.getOfficerAppointments(OFFICER_ID, null, null, null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(officerAppointments, response.getBody());
        verify(service).getOfficerAppointments(request);
    }

    @Test
    @DisplayName("Call to get officer appointments returns http 404 not found when officer id does not exist")
    void testGetOfficerAppointmentsNotFound() {
        // given
        OfficerAppointmentsRequest request = new OfficerAppointmentsRequest(OFFICER_ID, null, null, null);
        when(service.getOfficerAppointments(any())).thenReturn(Optional.empty());

        // when
        ResponseEntity<AppointmentList> response = controller.getOfficerAppointments(OFFICER_ID, null, null, null);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(service).getOfficerAppointments(request);
        verify(logger).error(loggerCaptor.capture());
        assertEquals(String.format("No appointments found for officer id %s", OFFICER_ID), loggerCaptor.getValue());
    }
}
