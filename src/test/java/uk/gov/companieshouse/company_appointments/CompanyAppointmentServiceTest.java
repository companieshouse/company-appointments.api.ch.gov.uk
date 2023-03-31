package uk.gov.companieshouse.company_appointments;

import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentService;
import uk.gov.companieshouse.company_appointments.service.CompanyRegisterService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uk.gov.companieshouse.company_appointments.util.CompanyStatusValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    @Mock
    private CompanyRegisterService companyRegisterService;

    private CompanyAppointmentMapper companyAppointmentMapper;

    @Mock
    private CompanyStatusValidator companyStatusValidator;

    @Mock
    private CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository;

    @Mock
    private ResourceChangedApiService resourceChangedApiService;

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


    @Captor
    private ArgumentCaptor<Sort> sortCaptor;

    @BeforeEach
    void setUp() throws Exception {
        companyAppointmentMapper = new CompanyAppointmentMapper();
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository, companyAppointmentMapper, sortMapper,
                companyRegisterService, companyStatusValidator, fullRecordAppointmentRepository, resourceChangedApiService, clock);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        // given
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(officerData));

        // when
        CompanyAppointmentView result = companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(CompanyAppointmentView.class, result.getClass());
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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, FILTER, ORDER_BY, null, null, null, null);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readAllByCompanyNumber(any(), any()))
                .thenReturn(new ArrayList<>());

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", ORDER_BY, null, null, null, null);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyNumberReturnsMappedAppointmentData() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", ORDER_BY, null, null, null, null);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumber(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyWhenNoParametersThenReturnsFirstThirtyFiveOfficers() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, null, null, null, null);

        assertEquals(35, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenItemsPerPageIsLargerThanOneHundredThenReturnsOneHundredBack() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, null, 150, null, null);

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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, null, 5, null, null);

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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, 5, null, null, null);


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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, 56, 15, null, null);


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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, 195, 50, null, null);


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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        assertThrows(NotFoundException.class,
                () -> {
                    AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                            "false", ORDER_BY, 300, null, null, null);
                });
    }

    @Test
    void testFetchAppointmentForCompanyReturnInactiveOrActiveCountInCompanyOfficersGETDependingOnCompanyStatusReturnsActive() throws Exception{
        OfficerData officer = officerData().build();
        officer.setResignedOn(null);
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officer, "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", ORDER_BY, null, null, null, null);

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

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", ORDER_BY, null, null, null, null);

        assertEquals(1, result.getInactiveCount());
        assertEquals(0, result.getActiveCount());

    }
    @Test
    void testNoRegisterViewIsFalse() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, null, null,
                null, null, false, null);

        verify(companyRegisterService, times(0)).isRegisterHeldInCompaniesHouse(any(), any());
    }

    @Test
    void testNoRegisterViewIsNull() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, null, null,
                null, null, null, null);

        verify(companyRegisterService, times(0)).isRegisterHeldInCompaniesHouse(any(), any());
    }
    @Test
    void testFetchAppointmentsForCompanyThrowsNotFoundExceptionIfRegisterViewAndNotHeldInCompaniesHouse() throws Exception {
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(REGISTER_TYPE, COMPANY_NUMBER)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> {
                    AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, null, null,
                            null, null, true, REGISTER_TYPE);
                });
    }

    @Test
    void testFetchAppointmentsForCompanyThrowsNotFoundExceptionIfNoAppointmentOfRegisterType() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", COMPANY_NUMBER)).thenReturn(true);

        assertThrows(NotFoundException.class,
                () -> {
                    AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, null, null,
                            null, null, true, "secretaries");
                });
    }

    @Test
    void testFetchAppointmentsForCompanyReturnsAppointmentsIfRegisterTypeMatches() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build(), "active");

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(null)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);
        when(companyRegisterService.isRegisterHeldInCompaniesHouse(REGISTER_TYPE, COMPANY_NUMBER)).thenReturn(true);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, null, null,
                null, null, true, REGISTER_TYPE);

        assertEquals(1, result.getItems().size());
    }

    @Test
    void testBadRequestExceptionThrownWhenCompanyNameIsMissing() {
        // given

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID, "", OPEN_STATUS, CONTEXT_ID);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals("Request missing mandatory fields: company name and/or company status", exception.getMessage());
    }

    @Test
    void testBadRequestExceptionThrownWhenCompanyStatusIsMissing() {
        // given

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, "", CONTEXT_ID);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals("Request missing mandatory fields: company name and/or company status", exception.getMessage());
    }

    @Test
    void testBadRequestExceptionThrownWhenInvalidStatusPassedToValidator() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(FAKE_STATUS)).thenReturn(false);

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, FAKE_STATUS, CONTEXT_ID);

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals("Non-valid company status provided", exception.getMessage());
    }

    @Test
    void testNotFoundExceptionThrownWhenAppointmentIsNotPresent() {
        // given
        when(companyStatusValidator.isValidCompanyStatus(OPEN_STATUS)).thenReturn(true);
        when(fullRecordAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(Optional.empty());

        // when
        Executable executable = () -> companyAppointmentService.patchNewAppointmentCompanyNameStatus(COMPANY_NUMBER, APPOINTMENT_ID, COMPANY_NAME, OPEN_STATUS, CONTEXT_ID);

        // then
        NotFoundException exception = assertThrows(NotFoundException.class, executable);
        assertEquals("Appointment [345678] for company [123456] not found", exception.getMessage());
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