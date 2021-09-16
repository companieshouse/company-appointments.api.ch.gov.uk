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
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentV2ControllerTest {
    private CompanyAppointmentV2Controller companyAppointmentV2Controller;

    @Mock
    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentView companyAppointmentView;

    @Mock
    private AppointmentAPI appointment;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";

    @BeforeEach
    void setUp() {
        companyAppointmentV2Controller = new CompanyAppointmentV2Controller(companyAppointmentService);
    }

    @Test
    void testControllerReturns200StatusAndCompanyAppointmentsData() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(companyAppointmentView);

        // when
        ResponseEntity<CompanyAppointmentView> response = companyAppointmentV2Controller.fetchAppointment(COMPANY_NUMBER,
            APPOINTMENT_ID);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(companyAppointmentView, response.getBody());
        verify(companyAppointmentService).fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testControllerReturns404StatusIfAppointmentNotFound() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointment(any(), any())).thenThrow(NotFoundException.class);

        // when
        ResponseEntity<CompanyAppointmentView> response = companyAppointmentV2Controller.fetchAppointment(COMPANY_NUMBER,
            APPOINTMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointment(any(), any());
    }

    @Test
    void testControllerReturns200WhenDataSubmitted() {
        // given
        when(companyAppointmentService.putAppointmentData(appointment)).thenReturn(appointment);

        // when
        ResponseEntity<Void> response = companyAppointmentV2Controller.submitOfficerData(appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
