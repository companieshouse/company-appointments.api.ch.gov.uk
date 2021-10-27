package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordControllerTest {
    private CompanyAppointmentFullRecordController companyAppointmentFullRecordController;

    @Mock
    private CompanyAppointmentFullRecordService companyAppointmentService;

    @Mock
    private AppointmentApiEntity appointment;

    @Mock
    private CompanyAppointmentFullRecordView appointmentView;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";

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
        ResponseEntity<Void> response = companyAppointmentFullRecordController.submitOfficerData(appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
