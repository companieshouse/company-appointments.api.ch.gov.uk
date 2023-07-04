package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import java.time.Clock;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Sort;
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;
import uk.gov.companieshouse.company_appointments.service.CompanyRegisterService;
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;
import uk.gov.companieshouse.company_appointments.util.CompanyStatusValidator;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    @Mock
    private CompanyRegisterService companyRegisterService;

    @Mock
    private CompanyStatusValidator companyStatusValidator;

    @Mock
    private CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository;

    @Mock
    private Clock clock;

    @Mock
    private SortMapper sortMapper;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";
    private final static String FILTER = "active";
    private final static String ORDER_BY = "surname";
    private final static Sort SORT = Sort.by("test");
    private final static String REGISTER_TYPE = "directors";
    private final static String COMPANY_NAME = "ACME LTD";
    private final static String OPEN_STATUS = "open";
    private final static String FAKE_STATUS = "fake";
    private final static String CONTEXT_ID = "ABC123";

    @BeforeEach
    void setUp() {
        CompanyAppointmentMapper companyAppointmentMapper = new CompanyAppointmentMapper();
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository,
                companyAppointmentMapper, sortMapper,
                companyRegisterService, companyStatusValidator, fullRecordAppointmentRepository,
                clock);
        MDC.put(REQUEST_ID.value(), CONTEXT_ID);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().withEtag("etag").build(), "active");

        // given
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(officerData));

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
    void testFetchAppointmentForCompanyNumberForNotResignedReturnsMappedAppointmentData() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter(FILTER)
                        .withOrderBy(ORDER_BY)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(OfficerList.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(companyAppointmentRepository.readAllByCompanyNumber(any(), any()))
                .thenReturn(new ArrayList<>());

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(request);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyNumberReturnsMappedAppointmentData() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getTotalResults());
        assertEquals(OfficerList.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumber(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyWhenNoParametersThenReturnsFirstThirtyFiveOfficers() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(35, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenItemsPerPageIsLargerThanOneHundredThenReturnsOneHundredBack() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .withItemsPerPage(150)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(100, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenItemsPerPageIsFiveThenReturnsFirstFiveOfficers() throws Exception {
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .withItemsPerPage(5)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(5, result.getTotalResults());
        assertEquals(String.valueOf(0), result.getItems().get(0).getOccupation());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexIsFiveThenReturnsThirtyFiveOfficersStartingFromIndexFive() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .withStartIndex(5)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(String.valueOf(5), result.getItems().get(0).getOccupation());
        assertEquals(35, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexAndItemsPerPagePresentThenReturnsItemsPerPageStartingFromStartIndex() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .withStartIndex(56)
                        .withItemsPerPage(15)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);


        assertEquals(String.valueOf(56), result.getItems().get(0).getOccupation());
        assertEquals(15, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexPlusItemsPerPageIsLargerThanSizeOfListThenReturnsItemsFromStartIndexToEndOfList() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .withStartIndex(195)
                        .withItemsPerPage(50)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);


        assertEquals(String.valueOf(195), result.getItems().get(0).getOccupation());
        assertEquals(5, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexIsLargerThanSizeOfListThrowsNotFoundException() throws Exception {
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");
            allAppointmentData.add(officerData);
        }

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("false")
                        .withOrderBy(ORDER_BY)
                        .withStartIndex(300)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
    }

    @Test
    void testFetchAppointmentForCompanyReturnInactiveOrActiveCountInCompanyOfficersGETDependingOnCompanyStatusReturnsActive() throws Exception{
        OfficerData officer = officerData().build();
        officer.setResignedOn(null);
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officer, "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getActiveCount());
        assertEquals(0, result.getInactiveCount());

    }

    @Test
    void testFetchAppointmentForCompanyReturnInactiveOrActiveCountInCompanyOfficersGETDependingOnCompanyStatusReturnsInactive() throws Exception{
        OfficerData officer = officerData().build();
        officer.setResignedOn(null);
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officer, "removed");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withFilter("filter")
                        .withOrderBy(ORDER_BY)
                        .build();

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getInactiveCount());
        assertEquals(0, result.getActiveCount());

    }
    @Test
    void testNoRegisterViewIsFalse() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(false)
                        .build();

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        companyAppointmentService.fetchAppointmentsForCompany(request);

        verify(companyRegisterService, times(0)).isRegisterHeldInCompaniesHouse(any(), any());
    }

    @Test
    void testNoRegisterViewIsNull() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .build();

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        companyAppointmentService.fetchAppointmentsForCompany(request);

        verify(companyRegisterService, times(0)).isRegisterHeldInCompaniesHouse(any(), any());
    }
    @Test
    void testFetchAppointmentsForCompanyThrowsNotFoundExceptionIfRegisterViewAndNotHeldInCompaniesHouse() throws Exception {
        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE)
                        .build();

        when(companyRegisterService.isRegisterHeldInCompaniesHouse(REGISTER_TYPE, COMPANY_NUMBER)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
    }

    @Test
    void testFetchAppointmentsForCompanyThrowsNotFoundExceptionIfNoAppointmentOfRegisterType() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType("secretaries")
                        .build();

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", COMPANY_NUMBER)).thenReturn(true);

        assertThrows(NotFoundException.class,
                () -> companyAppointmentService.fetchAppointmentsForCompany(request));
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatches() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        FetchAppointmentsRequest request =
                FetchAppointmentsRequest.Builder.builder()
                        .withCompanyNumber(COMPANY_NUMBER)
                        .withRegisterView(true)
                        .withRegisterType(REGISTER_TYPE)
                        .build();

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(REGISTER_TYPE, COMPANY_NUMBER)).thenReturn(true);

        OfficerList result = companyAppointmentService.fetchAppointmentsForCompany(request);

        assertEquals(1, result.getItems().size());
    }

    @Test
    @DisplayName("Test update a companies appointments")
    void shouldUpdateCompanyAppointments() throws Exception {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(fullRecordAppointmentRepository.patchAppointmentNameStatusInCompany(anyString(),
                anyString(), anyString(), any(), anyString())).thenReturn(2L);

        // when
        companyAppointmentService.patchCompanyNameStatus(COMPANY_NUMBER, COMPANY_NAME, OPEN_STATUS);

        // then
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(fullRecordAppointmentRepository).patchAppointmentNameStatusInCompany(
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
        verifyNoInteractions(fullRecordAppointmentRepository);
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
        verifyNoInteractions(fullRecordAppointmentRepository);
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
        verifyNoInteractions(fullRecordAppointmentRepository);
    }

    @DisplayName("Test throw ServiceUnavailableException when updating a companies appointments when and Mongo is down")
    @Test
    void shouldThrowServiceUnavailableExceptionWhenUpdatingCompanyAppointmentsAndMongoDown() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(fullRecordAppointmentRepository.patchAppointmentNameStatusInCompany(anyString(),
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
        verify(fullRecordAppointmentRepository).patchAppointmentNameStatusInCompany(
                eq(COMPANY_NUMBER),
                eq(COMPANY_NAME), eq(OPEN_STATUS), any(), anyString());
    }

    @DisplayName("Test throw NotFoundException when updating a companies appointments when company number not found")
    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingCompanyAppointmentsWhenCompanyNumberNotFound() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(fullRecordAppointmentRepository.patchAppointmentNameStatusInCompany(anyString(),
                anyString(), anyString(), any(), anyString())).thenReturn(0L);
        // when
        Executable executable = () -> companyAppointmentService.patchCompanyNameStatus(
                COMPANY_NUMBER, COMPANY_NAME, OPEN_STATUS);

        // then
        NotFoundException exception = assertThrows(NotFoundException.class, executable);
        assertEquals("No appointments found for company [123456] during PATCH request",
                exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(fullRecordAppointmentRepository).patchAppointmentNameStatusInCompany(
                eq(COMPANY_NUMBER),
                eq(COMPANY_NAME), eq(OPEN_STATUS), any(), anyString());
    }

    @Test
    @DisplayName("Should update appointment with no exceptions thrown")
    void patchNewAppointmentNameStatusSuccessfulUpdate() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(fullRecordAppointmentRepository.patchAppointmentNameStatus(anyString(), anyString(),
                anyString(), any(), anyString())).thenReturn(1L);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(
                COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, OPEN_STATUS);

        // then
        assertDoesNotThrow(executable);
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(fullRecordAppointmentRepository).patchAppointmentNameStatus(any(), any(), any(),
                any(), any());
    }

    @Test
    @DisplayName("Should throw bad request exception when company name is missing from request")
    void patchNewAppointmentNameStatusMissingCompanyName() {
        // given

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID, "", OPEN_STATUS);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals(
                "Request failed for company [123456] with appointment [345678], contextId: [ABC123]: company name and/or company status missing.",
                exception.getMessage());
        verifyNoInteractions(companyStatusValidator);
        verifyNoInteractions(fullRecordAppointmentRepository);
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
        verifyNoInteractions(fullRecordAppointmentRepository);
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
        verifyNoInteractions(fullRecordAppointmentRepository);
    }

    @Test
    @DisplayName("Should throw not found exception when cannot locate existing appointment")
    void patchNewAppointmentNameStatusMissingAppointmentAfterResourceChanged() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(OPEN_STATUS)).thenReturn(true);
        when(fullRecordAppointmentRepository.patchAppointmentNameStatus(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(0L);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, OPEN_STATUS);

        // then
        NotFoundException exception = assertThrows(NotFoundException.class, executable);
        assertEquals("Appointment [345678] for company [123456] not found during PATCH request", exception.getMessage());
        verify(companyStatusValidator).isValidCompanyStatus(OPEN_STATUS);
        verify(fullRecordAppointmentRepository).patchAppointmentNameStatus(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw service unavailable exception when MongoDB is unavailable")
    void patchNewAppointmentNameStatusMongoUnavailableAfterResourceChanged() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(anyString())).thenReturn(true);
        when(fullRecordAppointmentRepository.patchAppointmentNameStatus(any(), any(), any(), any(),
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
        verify(fullRecordAppointmentRepository).patchAppointmentNameStatus(any(), any(), any(),
                any(), any());
    }

    private OfficerData.Builder officerData() {
        return OfficerData.builder()
                .withAppointedOn(LocalDateTime.of(2020, 8, 26, 12, 0))
                .withResignedOn(LocalDateTime.of(2020, 8, 26, 13, 0))
                .withCountryOfResidence("Country")
                .withDateOfBirth(LocalDateTime.of(1980, 1, 1, 12, 0))
                .withLinks(new LinksData("/company/12345678/appointment/123", "/officers/abc", "/officers/abc/appointments"))
                .withNationality("Nationality")
                .withOccupation("Occupation")
                .withOfficerRole("director")
                .withForename("John")
                .withSurname("Doe")
                .withServiceAddress(ServiceAddressData.builder()
                        .withAddressLine1("Address 1")
                        .withAddressLine2("Address 2")
                        .withCareOf("Care of")
                        .withCountry("Country")
                        .withLocality("Locality")
                        .withPostcode("AB01 9XY")
                        .withPoBox("PO Box")
                        .withPremises("Premises")
                        .withRegion("Region")
                        .build())
                .withResponsibilities("responsibilities")
                .withPrincipalOfficeAddress(ServiceAddressData.builder()
                        .withAddressLine1("Address 1")
                        .withAddressLine2("Address 2")
                        .withCareOf("Care of")
                        .withCountry("Country")
                        .withLocality("Locality")
                        .withPostcode("AB01 9XY")
                        .withPoBox("PO Box")
                        .withPremises("Premises")
                        .withRegion("Region")
                        .build())
                .withContactDetails(ContactDetailsData.builder()
                        .withContactName("Name")
                        .build());
    }
}