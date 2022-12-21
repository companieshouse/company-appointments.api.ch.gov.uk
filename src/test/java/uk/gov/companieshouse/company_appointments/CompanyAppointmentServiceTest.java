package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    private CompanyAppointmentMapper companyAppointmentMapper;

    @Mock
    private SortMapper sortMapper;

    private final static String COMPANY_NUMBER = "123456";

    private final static String APPOINTMENT_ID = "345678";

    private final static String FILTER = "active";

    private final static String ORDER_BY = "surname";

    private final static Sort SORT = Sort.by("test");

    @Captor
    private ArgumentCaptor<Sort> sortCaptor;

    @BeforeEach
    void setUp() throws Exception {
        companyAppointmentMapper = new CompanyAppointmentMapper();
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository, companyAppointmentMapper, sortMapper);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

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
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, FILTER, ORDER_BY, null, null);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readAllByCompanyNumber(any(), any()))
                .thenReturn(new ArrayList<>());

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", ORDER_BY, null, null);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyNumberReturnsMappedAppointmentData() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", ORDER_BY, null, null);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumber(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyWhenNoParametersThenReturnsFirstThirtyFiveOfficers() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, null, null);

        assertEquals(35, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenItemsPerPageIsLargerThanOneHundredThenReturnsOneHundredBack() throws Exception {
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, null, 150);

        assertEquals(100, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenItemsPerPageIsFiveThenReturnsFirstFiveOfficers() throws Exception {
        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++){
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, null, 5);

        assertEquals(5, result.getTotalResults());
        assertEquals(String.valueOf(0), result.getItems().get(0).getOccupation());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexIsFiveThenReturnsThirtyFiveOfficersStartingFromIndexFive() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, 5, null);


        assertEquals("5", result.getItems().get(0).getOccupation());
        assertEquals(35, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexAndItemsPerPagePresentThenReturnsItemsPerPageStartingFromStartIndex() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, 56, 15);


        assertEquals("56", result.getItems().get(0).getOccupation());
        assertEquals(15, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexPlusItemsPerPageIsLargerThanSizeOfListThenReturnsItemsFromStartIndexToEndOfList() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());
            officerData.getData().setOccupation(String.valueOf(i));
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                "false", ORDER_BY, 195, 50);


        assertEquals("195", result.getItems().get(0).getOccupation());
        assertEquals(5, result.getTotalResults());
    }

    @Test
    void testFetchAppointmentForCompanyWhenStartIndexIsLargerThanSizeOfListThrowsNotFoundException() throws Exception {

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());
            allAppointmentData.add(officerData);
        }

        when(sortMapper.getSort(ORDER_BY)).thenReturn(SORT);
        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);

        assertThrows(NotFoundException.class,
                () -> {
                    AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER,
                            "false", ORDER_BY, 300, null);
                });
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
                .withOfficerRole("Role")
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
