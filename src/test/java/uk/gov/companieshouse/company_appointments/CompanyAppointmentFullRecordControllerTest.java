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
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.controller.CompanyAppointmentFullRecordController;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordControllerTest {
    private CompanyAppointmentFullRecordController companyAppointmentFullRecordController;

    @Mock
    private CompanyAppointmentFullRecordService companyAppointmentService;

    @Mock
    private FullRecordCompanyOfficerApi appointment;

    @Mock
    private CompanyAppointmentFullRecordView appointmentView;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";
    private final static String CONTEXT_ID = "contextId";

    @BeforeEach
    void setUp() {
        companyAppointmentFullRecordController = new CompanyAppointmentFullRecordController(companyAppointmentService);
    }

    @Test
    void testControllerReturns200StatusAndCompanyAppointmentsData() throws NotFoundException {
        // given
        when(companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(appointmentView);

        // when
        ResponseEntity<CompanyAppointmentFullRecordView> response = companyAppointmentFullRecordController.getAppointment(COMPANY_NUMBER,
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
        ResponseEntity<CompanyAppointmentFullRecordView> response = companyAppointmentFullRecordController.getAppointment(COMPANY_NUMBER,
                APPOINTMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).getAppointment(any(), any());
    }

    @Test
    void testControllerReturns200WhenDataSubmitted() {
        // given

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.submitOfficerData(CONTEXT_ID, appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testControllerReturns503StatusWhenPutEndpointIsCalled() throws Exception {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(companyAppointmentService).upsertAppointmentDelta(any(), any());

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.submitOfficerData(CONTEXT_ID, appointment);

        // then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void testControllerReturns200WhenOfficerDeleted() {

        ResponseEntity<Void> response = companyAppointmentFullRecordController.deleteOfficerData(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testControllerReturns404WhenOfficerNotDeleted() throws Exception{
        doThrow(NotFoundException.class).when(companyAppointmentService).deleteAppointmentDelta(any(), any(), any());

        ResponseEntity<Void> response = companyAppointmentFullRecordController.deleteOfficerData(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testControllerReturns503StatusWhenDeleteEndpointIsCalled() throws ServiceUnavailableException, NotFoundException {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(companyAppointmentService).deleteAppointmentDelta(any(), any(), any());

        // when
        ResponseEntity<Void> response = companyAppointmentFullRecordController.deleteOfficerData(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
