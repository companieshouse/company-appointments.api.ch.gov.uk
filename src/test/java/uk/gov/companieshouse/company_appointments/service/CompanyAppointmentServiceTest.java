package uk.gov.companieshouse.company_appointments.service;

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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.LinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.OfficerList.KindEnum;
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
    private Clock clock;

    @Mock
    private MetricsApi metricsApi;

    private static final String COMPANY_NUMBER = "123456";
    private static final String APPOINTMENT_ID = "345678";
    private static final String ACTIVE = "active";
    private static final String ORDER_BY = "surname";
    private static final String REGISTER_TYPE_DIRECTORS = "directors";
    private static final String REGISTER_TYPE_SECRETARIES = "secretaries";
    private static final String REGISTER_TYPE_LLPMEMBERS = "llp_members";
    private static final String COMPANY_NAME = "ACME LTD";
    private static final String OPEN_STATUS = "open";
    private static final String FAKE_STATUS = "fake";

    @BeforeEach
    void setUp() {
        CompanyAppointmentMapper companyAppointmentMapper = new CompanyAppointmentMapper();
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository,
                companyAppointmentMapper,
                companyRegisterService,
                companyMetricsApiService
        );
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().build(),
                ACTIVE);

        // given
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(appointmentDocument));

        // when
        OfficerSummary result = companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(OfficerSummary.class, result.getClass());
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
    void testFetchAppointmentsForCompanyNumberReturnsMappedAppointmentData() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);
        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withOrderBy(ORDER_BY)
                        .build();

        CountsApi countsApi = new CountsApi().appointments(new AppointmentsApi()
                .totalCount(1)
                .activeCount(1)
                .resignedCount(0));

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(countsApi);
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getActiveCount());
        assertEquals(0, result.getInactiveCount());
        assertEquals(0, result.getResignedCount());
        assertEquals(OfficerList.class, result.getClass());
        assertEquals("etag", result.getEtag());
        verify(companyAppointmentRepository).getCompanyAppointments(eq(COMPANY_NUMBER),
                eq(ORDER_BY), isNull(), eq(0), eq(35), eq(false), eq(false));
    }

    @Test
    void testFetchAppointmentsForCompanyWithActiveCompanyStatusHasZeroInactiveAppointments() {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument officerData = buildCompanyAppointmentDocument(officer, ACTIVE);

        List<CompanyAppointmentDocument> allAppointmentData = List.of(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
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
    void testFetchAppointmentsForCompanyWithAnInactiveCompanyStatusHasZeroActiveAppointments() {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officer, "closed");

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(2)
                .activeCount(2) // metrics returns active even when considered inactive
                .resignedCount(0)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(2, result.getTotalResults());
        assertEquals(2, result.getInactiveCount());
        assertEquals(0, result.getActiveCount());
    }

    @Test
    void shouldReturnZeroCountsForCompanyWithAnInactiveCompanyStatusAsHasZeroActiveAppointmentsWhenFilterIsApplied() {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withOrderBy(ORDER_BY)
                        .withFilter(ACTIVE)
                        .build();

        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(Collections.emptyList());

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(buildBaseResponse(), result);
    }

    @Test
    void testFetchAppointmentsForCompanyReturnEmptyResponseWhenCompanyIsInactiveAndFilterIsApplied() {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter(ACTIVE)
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(Collections.emptyList());

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(buildBaseResponse(), result);
    }

    @Test
    void testFetchAppointmentsForCompanyThrowsBadRequestExceptionWhenInvalidFilterSupplied() {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("incorrect filter")
                        .build();

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
        assertEquals("Invalid filter parameter supplied: incorrect filter",
                exception.getMessage());
    }

    @Test
    void shouldReturnTotalCountEqualToActiveCountWhenCompanyStatusIsActiveAndActiveFilterIsApplied() {
        DeltaOfficerData officer = buildOfficerData()
                .resignedOn(null)
                .build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officer, ACTIVE);

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter(ACTIVE)
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi().appointments(new AppointmentsApi()
                .totalCount(3)
                .activeCount(2)
                .resignedCount(1)));
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(2, result.getTotalResults());
        assertEquals(0, result.getInactiveCount());
        assertEquals(2, result.getActiveCount());
        assertEquals(1, result.getResignedCount());
    }

    @Test
    void testRegisterViewIsFalseShouldNotCheckMetricsForRegisterView() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);

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
    void testNoRegisterViewIsNullShouldNotCheckMetricsForRegisterView() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);

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
            throws ServiceUnavailableException, NotFoundException {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE_DIRECTORS)
                        .build();

        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(eq(REGISTER_TYPE_DIRECTORS), any())).thenReturn(
                false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
        assertEquals("Register not held at Companies House", exception.getMessage());
    }

    @Test
    void testFetchAppointmentsForCompanyThrowsBadRequestExceptionIfRegisterViewAndInvalidRegisterTypeFindingCounts()
            throws ServiceUnavailableException, NotFoundException {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType("invalid register type")
                        .build();

        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi()
                .appointments(new AppointmentsApi()
                        .totalCount(3)
                        .activeCount(2)
                        .resignedCount(1)));
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(any(), any())).thenReturn(
                true);
        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
        assertEquals("Incorrect register type, must be directors, secretaries or llp_members", exception.getMessage());
    }

    @Test
    void testFetchAppointmentsForCompanyThrowsNotFoundExceptionIfNoCountsInMetrics()
            throws ServiceUnavailableException, NotFoundException {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .build();

        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(request);

        NotFoundException exception = assertThrows(NotFoundException.class, result);
        assertEquals("Appointments metrics for company number [" + COMPANY_NUMBER + "] not found",
                exception.getMessage());
    }

    @Test
    void throwNotFoundExceptionIfCountsAppointmentsIsNull() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(buildOfficerData().build(),
                ACTIVE);

        List<CompanyAppointmentDocument> allAppointmentData = List.of(appointmentDocument);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .build();

        when(companyAppointmentRepository.getCompanyAppointments(any(), any(), any(), anyInt(),
                anyInt(), anyBoolean(), anyBoolean())).thenReturn(allAppointmentData);
        when(companyMetricsApiService.invokeGetMetricsApi(anyString())).thenReturn(
                new ApiResponse<>(200, null, metricsApi));
        when(metricsApi.getCounts()).thenReturn(new CountsApi());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
        assertEquals("Appointments metrics for company number [" + COMPANY_NUMBER + "] not found",
                exception.getMessage());
        verifyNoMoreInteractions(companyAppointmentRepository);
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatchesDirectorsAndRoleTypeIsDirector() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("director").build(), ACTIVE);

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
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatchesSecretariesAndRoleTypeIsSecretary() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("secretary").build(), ACTIVE);

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
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatchesLLPMembersAndRoleTypeIsLLPMember() {
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(
                buildOfficerData().officerRole("llp-member").build(), ACTIVE);

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
                        .setCareOf("Care of")
                        .setCountry("Country")
                        .setLocality("Locality")
                        .setPostalCode("AB01 9XY")
                        .setPoBox("PO Box")
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
                .contactDetails(new DeltaContactDetails().setContactName("Name"))
                .etag("etag");
    }

    private DeltaSensitiveData buildSensitiveData() {
        return new DeltaSensitiveData()
                .setDateOfBirth(LocalDateTime.of(1980, 1, 1, 12, 0).toInstant(ZoneOffset.UTC));
    }

    private static OfficerList buildBaseResponse() {
        return new OfficerList()
                .totalResults(0)
                .items(Collections.emptyList())
                .activeCount(0)
                .inactiveCount(0)
                .resignedCount(0)
                .kind(KindEnum.OFFICER_LIST)
                .startIndex(0)
                .itemsPerPage(35)
                .links(new LinkTypes())
                .etag("");
    }
}