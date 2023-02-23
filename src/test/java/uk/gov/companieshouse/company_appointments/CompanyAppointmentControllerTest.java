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

import uk.gov.companieshouse.company_appointments.controller.CompanyAppointmentController;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;

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
    void testControllerReturns200StatusAndAppointmentsForCompany() throws Exception {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, null, null, null, null)).thenReturn(allCompanyAppointmentsView);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null, null, null, null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(allCompanyAppointmentsView, response.getBody());
        verify(companyAppointmentService).fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, null, null, null, null);
    }

    @Test
    void testControllerReturns404StatusIfAppointmentForCompanyNotFound() throws Exception {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(any(), any(), any(), any(), any(), any(), any())).thenThrow(NotFoundException.class);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null, null, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testControllerReturns400StatusIfOrderByParameterIsIncorrect() throws Exception {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "false", "invalid", null, null, null, null)).thenThrow(BadRequestException.class);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", "invalid", null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(COMPANY_NUMBER, "false", "invalid", null, null, null, null);
    }
    @Test
    void testFetchAppointmentForCompanyWithIndexAndItemsReturns200Status() throws Exception {

        when(companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, 20, 50, null, null)).thenReturn(allCompanyAppointmentsView);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, 20, 50, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, 20, 50, null, null);
    }

    @Test
    void testControllerReturns500StatusIfThereIsServiceUnavailable() throws Exception {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, null, null, true, "directors"))
                .thenThrow(ServiceUnavailableException.class);

        // when
        ResponseEntity<AllCompanyAppointmentsView> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null, null, true, "directors");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(COMPANY_NUMBER, "false", null, null, null, true, "directors");
    }
}
