package uk.gov.companieshouse.company_appointments;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.company_appointments.controller.CompanyAppointmentController;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;

@ExtendWith(MockitoExtension.class)
public class CompanyAppointmentControllerTest {

    public static final String COMPANY_NAME = "NewCo";
    public static final String COMPANY_STATUS_ACTIVE = "active";
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

    @Test
    void shouldReturnOKForValidRequestWhenPatchingNameAndStatus() throws Exception {
        // Given

        // When
        ResponseEntity<Void> response = companyAppointmentController.patchCompanyNameStatus(
                COMPANY_NUMBER, new PatchAppointmentNameStatusApi()
                        .companyName(COMPANY_NAME).companyStatus(COMPANY_STATUS_ACTIVE));
        // Then
        verify(companyAppointmentService).patchCompanyNameStatus(COMPANY_NUMBER, COMPANY_NAME,
                COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldThrowNotFoundForMissingCompanyNumberWhenPatchingNameAndStatus() throws Exception {
        // Given
        doThrow(NotFoundException.class)
                .when(companyAppointmentService).patchCompanyNameStatus(any(), any(), any());

        // When
        ResponseEntity<Void> response = companyAppointmentController.patchCompanyNameStatus(
                COMPANY_NUMBER, new PatchAppointmentNameStatusApi()
                        .companyName(COMPANY_NAME).companyStatus(COMPANY_STATUS_ACTIVE));
        // Then
        verify(companyAppointmentService).patchCompanyNameStatus(COMPANY_NUMBER, COMPANY_NAME,
                COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldThrowBadRequestForMissingCompanyNameWhenPatchingNameAndStatus() throws Exception {
        // Given
        doThrow(BadRequestException.class)
                .when(companyAppointmentService).patchCompanyNameStatus(any(), any(), any());

        // When
        ResponseEntity<Void> response = companyAppointmentController.patchCompanyNameStatus(
                COMPANY_NUMBER, new PatchAppointmentNameStatusApi()
                        .companyStatus(COMPANY_STATUS_ACTIVE));
        // Then
        verify(companyAppointmentService).patchCompanyNameStatus(COMPANY_NUMBER, null,
                COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldThrowServiceUnavailableForWhenPatchingNameAndStatusAndMongoUnavailable()
            throws Exception {
        // Given
        doThrow(ServiceUnavailableException.class)
                .when(companyAppointmentService).patchCompanyNameStatus(any(), any(), any());

        // When
        ResponseEntity<Void> response = companyAppointmentController.patchCompanyNameStatus(
                COMPANY_NUMBER, new PatchAppointmentNameStatusApi()
                        .companyName(COMPANY_NAME)
                        .companyStatus(COMPANY_STATUS_ACTIVE));
        // Then
        verify(companyAppointmentService).patchCompanyNameStatus(COMPANY_NUMBER, COMPANY_NAME,
                COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void patchNewAppointmentCompanyNameStatusReturnsOKStatus() throws Exception {
        // given

        // when
        ResponseEntity<Void> response = companyAppointmentController.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID,
                new PatchAppointmentNameStatusApi().companyName(COMPANY_NAME)
                        .companyStatus(COMPANY_STATUS_ACTIVE));

        // then
        verify(companyAppointmentService).patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID,
                COMPANY_NAME, COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getLocation()).hasToString(String.format("/company/%s/appointments/%s", COMPANY_NUMBER, APPOINTMENT_ID));

    }

    @Test
    void patchNewAppointmentCompanyNameStatusReturnsBadRequestStatus() throws Exception {
        // given
        doThrow(BadRequestException.class)
                .when(companyAppointmentService).patchNewAppointmentCompanyNameStatus(any(), any(), any(), any());

        // when
        ResponseEntity<Void> response = companyAppointmentController.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID,
                new PatchAppointmentNameStatusApi().companyName(COMPANY_NAME).companyStatus(COMPANY_STATUS_ACTIVE));

        // then
        verify(companyAppointmentService).patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID,
                COMPANY_NAME, COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getLocation()).isNull();
    }

    @Test
    void patchNewAppointmentCompanyNameStatusReturnsNotFoundStatus() throws Exception {
        // given
        doThrow(NotFoundException.class)
                .when(companyAppointmentService).patchNewAppointmentCompanyNameStatus(any(), any(), any(), any());

        // when
        ResponseEntity<Void> response = companyAppointmentController.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID,
                new PatchAppointmentNameStatusApi().companyName(COMPANY_NAME).companyStatus(COMPANY_STATUS_ACTIVE));

        // then
        verify(companyAppointmentService).patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID,
                COMPANY_NAME, COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getLocation()).isNull();
    }

    @Test
    void patchNewAppointmentCompanyNameStatusReturnsInternalServerErrorStatus() throws Exception {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(companyAppointmentService).patchNewAppointmentCompanyNameStatus(any(), any(), any(), any());

        // when
        ResponseEntity<Void> response = companyAppointmentController.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID,
                new PatchAppointmentNameStatusApi().companyName(COMPANY_NAME).companyStatus(COMPANY_STATUS_ACTIVE));

        // then
        verify(companyAppointmentService).patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID,
                COMPANY_NAME, COMPANY_STATUS_ACTIVE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getLocation()).isNull();
    }
}