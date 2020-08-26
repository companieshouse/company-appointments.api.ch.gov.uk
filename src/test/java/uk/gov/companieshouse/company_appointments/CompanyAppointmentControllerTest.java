package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CompanyAppointmentControllerTest {

    private CompanyAppointmentController companyAppointmentController;

    @Mock
    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointment companyAppointment;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";

    @BeforeEach
    void setUp() {
        companyAppointmentController = new CompanyAppointmentController(companyAppointmentService);
    }

    @Test
    void testControllerReturns200StatusAndCompanyAppointmentsData() {
        // given
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(companyAppointment);

        // when
        ResponseEntity<CompanyAppointment> response = companyAppointmentController.fetchAppointment(COMPANY_NUMBER,
                APPOINTMENT_ID);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(companyAppointment, response.getBody());
        verify(companyAppointmentService).fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);
    }
}
