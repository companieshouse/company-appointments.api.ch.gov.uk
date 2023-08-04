package uk.gov.companieshouse.company_appointments.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.company_appointments.util.CompanyStatusValidator;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyRegisterService companyRegisterService;

    @Mock
    private CompanyMetricsApiService companyMetricsApiService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    @Mock
    private CompanyStatusValidator companyStatusValidator;

    @Mock
    private Clock clock;

    @Mock
    private MetricsApi metricsApi;

    private static final String COMPANY_NUMBER = "123456";
    private static final String APPOINTMENT_ID = "345678";
    private static final String FILTER = "active";
    private static final String ORDER_BY = "surname";
    private static final String REGISTER_TYPE_DIRECTORS = "directors";
    private static final String REGISTER_TYPE_SECRETARIES = "secretaries";
    private static final String REGISTER_TYPE_LLPMEMBERS = "llp_members";
    private static final String COMPANY_NAME = "ACME LTD";
    private static final String OPEN_STATUS = "open";
    private static final String FAKE_STATUS = "fake";
    private static final String CONTEXT_ID = "ABC123";

    @BeforeEach
    void setUp() {
        CompanyAppointmentMapper companyAppointmentMapper = new CompanyAppointmentMapper();
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository,
                companyAppointmentMapper,
                companyRegisterService,
                companyMetricsApiService,
                companyStatusValidator,
                clock);
        MDC.put(REQUEST_ID.value(), CONTEXT_ID);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().etag("etag").build(),
                "active");

        // given
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(appointmentDocument));

        // when
        OfficerSummary result = companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(OfficerSummary.class, result.getClass());
        assertEquals("etag", result.getEtag());
        verify(companyAppointmentRepository).readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testFetchAppointmentThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(any(), any()))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyThrowsNotFoundExceptionIfAppointmentDoesntExist()
            throws ServiceUnavailableException {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(new ArrayList<>());

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(request);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyNumberReturnsMappedAppointmentData() throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                "active");
        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));

        CountsApi countsApi = new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0));
        when(metricsApi.getCounts()).thenReturn(countsApi);
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getActiveCount());
        assertEquals(0, result.getInactiveCount());
        assertEquals(0, result.getResignedCount());
        assertEquals(OfficerList.class, result.getClass());
        verify(companyAppointmentRepository).getCompanyAppointments(eq(COMPANY_NUMBER),
                eq(ORDER_BY), isNull(), eq(0), eq(35), eq(false), eq(false));
    }

    @Test
    void testFetchAppointmentForCompanyReturnInactiveOrActiveCountInCompanyOfficersGETDependingOnCompanyStatusReturnsActive()
            throws Exception {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument officerData = buildCompanyAppointmentDocument(officer, "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getActiveCount());
        assertEquals(0, result.getInactiveCount());
    }

    @Test
    void testFetchAppointmentForCompanyReturnInactiveOrActiveCountInCompanyOfficersGETDependingOnCompanyStatusReturnsInactive()
            throws Exception {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officer, "removed");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getInactiveCount());
        assertEquals(0, result.getActiveCount());
    }

    @Test
    void testFetchAppointmentForCompanyReturnTotalCountOfZeroWhenCompanyStatusIsInactiveAndActiveFilterIsApplied()
            throws Exception {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officer, "dissolved");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter(FILTER)
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(0, result.getTotalResults());
        assertEquals(1, result.getInactiveCount());
        assertEquals(0, result.getActiveCount());
    }

    @Test
    void shouldReturnTotalCountEqualToActiveCountWhenCompanyStatusIsActiveAndActiveFilterIsApplied()
            throws Exception {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officer, "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter(FILTER)
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(0, result.getInactiveCount());
        assertEquals(1, result.getActiveCount());
    }

    @Test
    void testRegisterViewIsFalse() throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(false)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        companyAppointmentService.fetchAppointmentsForCompany(request);

        verify(companyRegisterService, never()).isRegisterHeldInCompaniesHouse(any(), any());
    }

    @Test
    void testNoRegisterViewIsNull() throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        companyAppointmentService.fetchAppointmentsForCompany(request);

        verify(companyRegisterService, never()).isRegisterHeldInCompaniesHouse(any(), any());
    }

    @Test
    void testFetchAppointmentsForCompanyThrowsNotFoundExceptionIfRegisterViewAndNotHeldInCompaniesHouse()
            throws ServiceUnavailableException {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE_DIRECTORS)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq(REGISTER_TYPE_DIRECTORS), any())).thenReturn(
                false);

        assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
    }

    @Test
    void throwNotFoundExceptionIfCountsAppointmentIsNull() throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType("secretaries")
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi());
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq("secretaries"), any())).thenReturn(true);

        assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
    }

    @Test
    void throwNotFoundExceptionIfCountsIsNull() throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType("secretaries")
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq("secretaries"), any())).thenReturn(true);

        assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatchesDirectorsAndRoleTypeIsDirector()
            throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("director").build(), "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE_DIRECTORS)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .activeDirectorsCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq(REGISTER_TYPE_DIRECTORS), any())).thenReturn(
                true);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatchesSecretariesAndRoleTypeIsSecretary()
            throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("secretary").build(), "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE_SECRETARIES)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .activeSecretariesCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq(REGISTER_TYPE_SECRETARIES), any())).thenReturn(
                true);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatchesLLPMembersAndRoleTypeIsLLPMember()
            throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("llp-member").build(), "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE_LLPMEMBERS)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .activeLlpMembersCount(1)
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq(REGISTER_TYPE_LLPMEMBERS), any())).thenReturn(
                true);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeDoesNotMatch() throws Exception {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("director").build(), "active");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType("registerType")
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(0)
                .resignedCount(1)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq("registerType"), any())).thenReturn(
                true);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(0, result.getActiveCount());
        assertEquals(1, result.getResignedCount());
    }

    @Test
    @DisplayName("Test update a companies appointments")
    void shouldUpdateCompanyAppointments() throws Exception {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(companyAppointmentRepository.patchAppointmentNameStatusInCompany(anyString(),
                anyString(), anyString(), any(), anyString())).thenReturn(2L);

        // when
        companyAppointmentService.patchCompanyNameStatus(COMPANY_NUMBER, COMPANY_NAME, OPEN_STATUS);

        // then
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(companyAppointmentRepository).patchAppointmentNameStatusInCompany(
                eq(COMPANY_NUMBER),
                eq(COMPANY_NAME), eq(OPEN_STATUS), any(), anyString());
    }

    @DisplayName("Test throw BadRequestException when updating a companies appointments when missing company name")
    @Test
    void shouldThrowBadRequestExceptionWhenUpdatingAppointmentsWhenMissingCompanyName() {
        // given
        // when
        Executable executable = () -> companyAppointmentService.patchCompanyNameStatus(
                COMPANY_NUMBER, "", OPEN_STATUS);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456], contextId: [ABC123]: company name and/or company status missing.",
                exception.getMessage());
        verifyNoInteractions(companyStatusValidator);
        verifyNoInteractions(companyAppointmentRepository);
    }

    @DisplayName("Test throw BadRequestException when updating a companies appointments when missing company status")
    @Test
    void shouldThrowBadRequestExceptionWhenUpdatingCompanyAppointmentsWhenMissingCompanyStatus() {
        // given
        // when
        Executable executable = () -> companyAppointmentService.patchCompanyNameStatus(
                COMPANY_NUMBER, COMPANY_NAME, "");

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456], contextId: [ABC123]: company name and/or company status missing.",
                exception.getMessage());
        verifyNoInteractions(companyStatusValidator);
        verifyNoInteractions(companyAppointmentRepository);
    }

    @DisplayName("Test throw BadRequestException when updating a companies appointments with invalid company status")
    @Test
    void shouldThrowBadRequestExceptionWhenUpdatingCompanyAppointmentsWithInvalidCompanyStatus() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(false);
        // when
        Executable executable = () -> companyAppointmentService.patchCompanyNameStatus(
                COMPANY_NUMBER, COMPANY_NAME, FAKE_STATUS);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456], contextId: [ABC123]: invalid company status provided.",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(FAKE_STATUS);
        verifyNoInteractions(companyAppointmentRepository);
    }

    @DisplayName("Test throw ServiceUnavailableException when updating a companies appointments when and Mongo is down")
    @Test
    void shouldThrowServiceUnavailableExceptionWhenUpdatingCompanyAppointmentsAndMongoDown() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(companyAppointmentRepository.patchAppointmentNameStatusInCompany(anyString(),
                anyString(), anyString(), any(), anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));
        // when
        Executable executable = () -> companyAppointmentService.patchCompanyNameStatus(
                COMPANY_NUMBER, COMPANY_NAME, OPEN_STATUS);

        // then
        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                executable);
        assertEquals(
                "Request failed for company [123456], contextId: [ABC123]: error connecting to MongoDB.",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(companyAppointmentRepository).patchAppointmentNameStatusInCompany(
                eq(COMPANY_NUMBER),
                eq(COMPANY_NAME), eq(OPEN_STATUS), any(), anyString());
    }

    @DisplayName("Test throw NotFoundException when updating a companies appointments when company number not found")
    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingCompanyAppointmentsWhenCompanyNumberNotFound() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(companyAppointmentRepository.patchAppointmentNameStatusInCompany(anyString(),
                anyString(), anyString(), any(), anyString())).thenReturn(0L);
        // when
        Executable executable = () -> companyAppointmentService.patchCompanyNameStatus(
                COMPANY_NUMBER, COMPANY_NAME, OPEN_STATUS);

        // then
        NotFoundException exception = assertThrows(NotFoundException.class, executable);
        assertEquals("No appointments found for company [123456] during PATCH request",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(companyAppointmentRepository).patchAppointmentNameStatusInCompany(
                eq(COMPANY_NUMBER),
                eq(COMPANY_NAME), eq(OPEN_STATUS), any(), anyString());
    }

    @Test
    @DisplayName("Should update appointment with no exceptions thrown")
    void patchNewAppointmentNameStatusSuccessfulUpdate() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(companyAppointmentRepository.patchAppointmentNameStatus(anyString(), anyString(),
                anyString(), any(), anyString())).thenReturn(1L);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, OPEN_STATUS);

        // then
        assertDoesNotThrow(executable);
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(companyAppointmentRepository).patchAppointmentNameStatus(any(), any(), any(),
                any(), any());
    }

    @Test
    @DisplayName("Should throw bad request exception when company name is missing from request")
    void patchNewAppointmentNameStatusMissingCompanyName() {
        // given

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER,
                APPOINTMENT_ID, "", OPEN_STATUS);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456] with appointment [345678], contextId: [ABC123]: company name and/or company status missing.",
                exception.getMessage());
        verifyNoInteractions(companyStatusValidator);
        verifyNoInteractions(companyAppointmentRepository);
    }

    @Test
    @DisplayName("Should throw bad request exception when company status is missing from request")
    void patchNewAppointmentNameStatusMissingCompanyStatus() {
        // given

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, "");

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456] with appointment [345678], contextId: [ABC123]: company name and/or company status missing.",
                exception.getMessage());
        verifyNoInteractions(companyStatusValidator);
        verifyNoInteractions(companyAppointmentRepository);
    }

    @Test
    @DisplayName("Should throw bad request exception when invalid company status is provided from request")
    void patchNewAppointmentNameStatusInvalidCompanyStatus() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(FAKE_STATUS)).thenReturn(false);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, FAKE_STATUS);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456] with appointment [345678], contextId: [ABC123]: invalid company status provided.",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(FAKE_STATUS);
        verifyNoInteractions(companyAppointmentRepository);
    }

    @Test
    @DisplayName("Should throw not found exception when cannot locate existing appointment")
    void patchNewAppointmentNameStatusMissingAppointmentAfterResourceChanged() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(OPEN_STATUS)).thenReturn(true);
        when(companyAppointmentRepository.patchAppointmentNameStatus(anyString(), anyString(), anyString(), any(),
                anyString())).thenReturn(0L);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER,
                APPOINTMENT_ID, COMPANY_NAME, OPEN_STATUS);

        // then
        NotFoundException exception = assertThrows(NotFoundException.class, executable);
        assertEquals("Appointment [345678] for company [123456] not found during PATCH request",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(companyAppointmentRepository).patchAppointmentNameStatus(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw service unavailable exception when MongoDB is unavailable")
    void patchNewAppointmentNameStatusMongoUnavailableAfterResourceChanged() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(companyAppointmentRepository.patchAppointmentNameStatus(any(), any(), any(), any(),
                any())).thenThrow(DataAccessResourceFailureException.class);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, OPEN_STATUS);

        // then
        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                executable);
        assertEquals(
                "Request failed for company [123456] with appointment [345678], contextId: [ABC123]: error connecting to MongoDB.",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(companyAppointmentRepository).patchAppointmentNameStatus(any(), any(), any(),
                any(), any());
    }

    private CompanyAppointmentDocument buildCompanyAppointmentDocument(DeltaOfficerData data,
            String status) {
        return new CompanyAppointmentDocument()
                .id("1")
                .data(data)
                .sensitiveData(buildSensitiveData())
                .companyStatus(status);
    }

    private DeltaOfficerData.Builder buildOfficerData() {
        return DeltaOfficerData.Builder.builder()
                .appointedOn(LocalDateTime.of(2020, 8, 26, 12, 0).toInstant(ZoneOffset.UTC))
                .resignedOn(LocalDateTime.of(2020, 8, 26, 13, 0).toInstant(ZoneOffset.UTC))
                .countryOfResidence("Country")
                .links(new DeltaItemLinkTypes()
                        .setSelf("/company/12345678/appointment/123")
                        .setOfficer(new DeltaOfficerLinkTypes()
                                .setSelf("/officers/abc")
                                .setAppointments("/officers/abc/appointments")))
                .nationality("Nationality")
                .occupation("Occupation")
                .officerRole("director")
                .forename("John")
                .surname("Doe")
                .serviceAddress(new DeltaServiceAddress()
                        .setAddressLine1("Address 1")
                        .setAddressLine2("Address 2")
//                        .setCareOf("Care of")
                        .setCountry("Country")
                        .setLocality("Locality")
                        .setPostalCode("AB01 9XY")
//                        .setPoBox("PO Box")
                        .setPremises("Premises")
                        .setRegion("Region"))
                .responsibilities("responsibilities")
                .principalOfficeAddress(new DeltaPrincipalOfficeAddress()
                        .setAddressLine1("Address 1")
                        .setAddressLine2("Address 2")
                        .setCareOf("Care of")
                        .setCountry("Country")
                        .setLocality("Locality")
                        .setPostalCode("AB01 9XY")
                        .setPoBox("PO Box")
                        .setPremises("Premises")
                        .setRegion("Region"))
                .contactDetails(new DeltaContactDetails().setContactName("Name"));
    }

    private DeltaSensitiveData buildSensitiveData() {
        return new DeltaSensitiveData()
                .setDateOfBirth(LocalDateTime.of(1980, 1, 1, 12, 0).toInstant(ZoneOffset.UTC));
    }
}