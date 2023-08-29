package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsControllerTest {

    private final static String OFFICER_ID = "567890";

    @InjectMocks
    private OfficerAppointmentsController controller;

    @Mock
    private OfficerAppointmentsService service;

    @Mock
    private ItemsPerPageService itemsPerPageService;

    @Mock
    private AppointmentList officerAppointments;

    @Test
    @DisplayName("Call to get officer appointments returns http 200 ok and officer appointments api")
    void testGetOfficerAppointments() throws BadRequestException {
        // given
        when(itemsPerPageService.getItemsPerPage(any(), anyString())).thenReturn(5);
        when(service.getOfficerAppointments(any())).thenReturn(Optional.of(officerAppointments));

        // when
        ResponseEntity<AppointmentList> response = controller.getOfficerAppointments(OFFICER_ID, null, 0, 5, "");

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(officerAppointments, response.getBody());
        verify(service).getOfficerAppointments(new OfficerAppointmentsRequest(OFFICER_ID, null, 0, 5));
    }

    @Test
    @DisplayName("Call to get officer appointments returns http 404 not found when officer id does not exist")
    void testGetOfficerAppointmentsNotFound() throws BadRequestException {
        // given
        when(itemsPerPageService.getItemsPerPage(any(), anyString())).thenReturn(35);
        when(service.getOfficerAppointments(any())).thenReturn(Optional.empty());

        // when
        ResponseEntity<AppointmentList> response = controller.getOfficerAppointments(OFFICER_ID, null, null, null, "");

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(service).getOfficerAppointments(new OfficerAppointmentsRequest(OFFICER_ID, null, null, 35));
    }

    @Test
    @DisplayName("Call to get officer appointments returns http 400 bad request when filter parameter is invalid")
    void testGetOfficerAppointmentsBadRequest() throws BadRequestException {
        // given
        when(itemsPerPageService.getItemsPerPage(any(), anyString())).thenReturn(35);
        when(service.getOfficerAppointments(any())).thenThrow(BadRequestException.class);

        // when
        ResponseEntity<AppointmentList> response = controller.getOfficerAppointments(OFFICER_ID, "invalid", null, null, "");

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(service).getOfficerAppointments(new OfficerAppointmentsRequest(OFFICER_ID, "invalid", null, 35));
    }
}
