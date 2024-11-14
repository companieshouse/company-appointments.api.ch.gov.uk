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
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;
import uk.gov.companieshouse.company_appointments.controller.CompanyAppointmentController;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;

@ExtendWith(MockitoExtension.class)
public class CompanyAppointmentControllerTest {

    public static final String COMPANY_NAME = "NewCo";
    public static final String COMPANY_STATUS_ACTIVE = "active";
    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";

    private CompanyAppointmentController companyAppointmentController;

    @Mock
    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private OfficerSummary officerSummary;

    @Mock
    private OfficerList officerList;

    @BeforeEach
    void setUp() {
        companyAppointmentController = new CompanyAppointmentController(companyAppointmentService);
    }

    @Test
    void testControllerReturns200StatusAndCompanyAppointmentsData() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(officerSummary);

        // when
        ResponseEntity<OfficerSummary> response = companyAppointmentController.fetchAppointment(COMPANY_NUMBER,
                APPOINTMENT_ID);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(officerSummary, response.getBody());
        verify(companyAppointmentService).fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testControllerReturns404StatusIfAppointmentNotFound() throws NotFoundException {
        // given
        when(companyAppointmentService.fetchAppointment(any(), any())).thenThrow(NotFoundException.class);

        // when
        ResponseEntity<OfficerSummary> response = companyAppointmentController.fetchAppointment(COMPANY_NUMBER,
                APPOINTMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointment(any(), any());
    }

    @Test
    void testControllerReturns200StatusAndAppointmentsForCompany() {
        // given
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .build();

        when(companyAppointmentService.fetchAppointmentsForCompany(any())).thenReturn(officerList);

        // when
        ResponseEntity<OfficerList> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null, null, null, null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(officerList, response.getBody());
        verify(companyAppointmentService).fetchAppointmentsForCompany(request);
    }

    @Test
    void testControllerReturns404StatusIfAppointmentForCompanyNotFound() {
        // given
        when(companyAppointmentService.fetchAppointmentsForCompany(any())).thenThrow(NotFoundException.class);

        // when
        ResponseEntity<OfficerList> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null, null, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(any());
    }

    @Test
    void testControllerReturns400StatusIfOrderByParameterIsIncorrect() {
        // given
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy("invalid")
                        .build();

        when(companyAppointmentService.fetchAppointmentsForCompany(any())).thenThrow(BadRequestException.class);

        // when
        ResponseEntity<OfficerList> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", "invalid", null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(request);
    }

    @Test
    void testFetchAppointmentForCompanyWithIndexAndItemsReturns200Status() {
        // given
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withStartIndex(20)
                        .withItemsPerPage(50)
                        .build();

        when(companyAppointmentService.fetchAppointmentsForCompany(any())).thenReturn(officerList);

        // when
        ResponseEntity<OfficerList> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, 20, 50, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(request);
    }

    @Test
    void testControllerReturns500StatusIfThereIsServiceUnavailable() {
        // given
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withRegisterView(true)
                        .withRegisterType("directors")
                        .build();

        when(companyAppointmentService.fetchAppointmentsForCompany(any()))
                .thenThrow(ServiceUnavailableException.class);

        // when
        ResponseEntity<OfficerList> response = companyAppointmentController.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", null, null, null, true, "directors");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(companyAppointmentService).fetchAppointmentsForCompany(request);
    }

    @Test
    void shouldReturnOKForValidRequestWhenPatchingNameAndStatus() {
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
    void shouldThrowNotFoundForMissingCompanyNumberWhenPatchingNameAndStatus() {
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
    void shouldThrowBadRequestForMissingCompanyNameWhenPatchingNameAndStatus() {
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
    void shouldThrowServiceUnavailableForWhenPatchingNameAndStatusAndMongoUnavailable() {
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
}