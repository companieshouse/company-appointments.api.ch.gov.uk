package uk.gov.companieshouse.company_appointments;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
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

import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

@ExtendWith(MockitoExtension.class)
public class CompanyAppointmentControllerTest {

    private CompanyAppointmentController companyAppointmentController;

    @Mock
    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentView companyAppointmentView;

    @Mock
    private AllCompanyAppointmentsView allCompanyAppointmentsView;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";

    @BeforeEach
    void setUp() {
        companyAppointmentController = new CompanyAppointmentController(companyAppointmentService);
    }

    @Test
    void testControllerReturns200StatusAndCompanyAppointmentsData() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(companyAppointmentView);

        // when
        ResponseEntity<CompanyAppointmentView> response = companyAppointmentController.fetchAppointment(COMPANY_NUMBER,
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
        ResponseEntity<CompanyAppointmentView> response = companyAppointmentController.fetchAppointment(COMPANY_NUMBER,
                APPOINTMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointment(any(), any());
    }

    @Test
    void testControllerReturns200StatusAndAppointmentsForCompany() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, null)).thenReturn(allCompanyAppointmentsView);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null,null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(allCompanyAppointmentsView, response.getBody());
        verify(companyAppointmentService).fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, null);
    }

    @Test
    void testControllerReturns404StatusIfAppointmentForCompanyNotFound() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(any(), any(), any(), any())).thenThrow(NotFoundException.class);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(any(), any(), any(), any());
    }

    @Test
    void testFetchAppointmentForCompanyWithIndexAndItemsReturns200Status() throws NotFoundException {

        when(companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "false", 20, 50)).thenReturn(allCompanyAppointmentsView);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", 20, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(COMPANY_NUMBER, "false", 20, 50);
    }

}
