package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.controller.CompanyAppointmentFullRecordController;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;
import uk.gov.companieshouse.company_appointments.service.DeleteAppointmentService;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordControllerTest {

    private static final String COMPANY_NUMBER = "123456";
    private static final String APPOINTMENT_ID = "345678";
    private static final String DELTA_AT = "20140925171003950844";
    private static final String OFFICER_ID = "officer_id";

    private CompanyAppointmentFullRecordController companyAppointmentFullRecordController;

    @Mock
    private CompanyAppointmentFullRecordService companyAppointmentService;

    @Mock
    private FullRecordCompanyOfficerApi appointment;

    @Mock
    private CompanyAppointmentFullRecordView appointmentView;

    @Mock
    private DeleteAppointmentService deleteAppointmentService;

    @BeforeEach
    void setUp() {
        companyAppointmentFullRecordController = new CompanyAppointmentFullRecordController(
                companyAppointmentService,
                deleteAppointmentService);
    }

    @Test
    void testControllerReturns200StatusAndCompanyAppointmentsData() throws NotFoundException {
        // given
        when(companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(appointmentView);

        // when
        ResponseEntity<CompanyAppointmentFullRecordView> response = companyAppointmentFullRecordController.getAppointment(
                COMPANY_NUMBER,
                APPOINTMENT_ID);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(appointmentView, response.getBody());
        verify(companyAppointmentService).getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testControllerReturns404StatusIfAppointmentNotFound() throws NotFoundException {
        // given
        when(companyAppointmentService.getAppointment(any(), any())).thenThrow(NotFoundException.class);

        // when
        ResponseEntity<CompanyAppointmentFullRecordView> response = companyAppointmentFullRecordController.getAppointment(
                COMPANY_NUMBER,
                APPOINTMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).getAppointment(any(), any());
    }

    @Test
    void testControllerReturns200WhenDataSubmitted() {
        // given
        when(appointment.getExternalData()).thenReturn(new ExternalData().companyNumber(COMPANY_NUMBER));

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.submitOfficerData(appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testControllerReturns400WhenDataSubmittedIsMissingCompanyNumber() {
        // given
        when(appointment.getExternalData()).thenReturn(new ExternalData());

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.submitOfficerData(appointment);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testControllerReturns503StatusWhenPutEndpointIsCalled() {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(companyAppointmentService).upsertAppointmentDelta(any());
        when(appointment.getExternalData()).thenReturn(new ExternalData().companyNumber(COMPANY_NUMBER));

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.submitOfficerData(appointment);

        // then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void testControllerReturns200WhenOfficerDeleted() {
        ResponseEntity<Void> response = companyAppointmentFullRecordController.deleteOfficerData(COMPANY_NUMBER,
                APPOINTMENT_ID, DELTA_AT, OFFICER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testControllerReturns404WhenOfficerNotDeleted() {
        doThrow(NotFoundException.class).when(deleteAppointmentService).deleteAppointment(any());

        ResponseEntity<Void> response = companyAppointmentFullRecordController.deleteOfficerData(COMPANY_NUMBER,
                APPOINTMENT_ID, DELTA_AT, OFFICER_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testControllerReturns503StatusWhenDeleteEndpointIsCalled() {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(deleteAppointmentService).deleteAppointment(any());

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.deleteOfficerData(COMPANY_NUMBER,
                APPOINTMENT_ID, DELTA_AT, OFFICER_ID);

        // then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
