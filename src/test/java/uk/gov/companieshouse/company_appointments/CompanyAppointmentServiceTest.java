package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    private CompanyAppointmentMapper companyAppointmentMapper;

    private final static String COMPANY_NUMBER = "123456";

    private final static String APPOINTMENT_ID = "345678";

    private final static String filter = "active";

    private final static Sort SORT = Sort.by("test");

    @Captor
    private ArgumentCaptor<Sort> sortCaptor;

    @BeforeEach
    void setUp() {
        companyAppointmentMapper = new CompanyAppointmentMapper();
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository, companyAppointmentMapper);
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


        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, filter, null);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumberForNotResigned(COMPANY_NUMBER, SORT);
    }

    @Test
    void testFetchAppointmentForCompanyThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readAllByCompanyNumber(any(), any()))
                .thenReturn(new ArrayList<>());

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", null);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyNumberReturnsMappedAppointmentData() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);


        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER, SORT))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", null);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumber(COMPANY_NUMBER, SORT);
    }

    @Test
    void testOfficerRoleSortOrder() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(companyAppointmentRepository.readAllByCompanyNumber(anyString(), sortCaptor.capture()))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", null);

        Sort expected = Sort.by(Sort.Direction.ASC, "officer_role_sort_order")
                        .and(Sort.by(Sort.Direction.ASC, "data.surname", "data.company_name"))
                        .and(Sort.by(Sort.Direction.ASC, "data.forename"))
                        .and(Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before"));

        assertEquals(expected, sortCaptor.getValue());
    }

    @Test
    void testOfficerRoleSortByAppointedOn() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(companyAppointmentRepository.readAllByCompanyNumber(anyString(), sortCaptor.capture()))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", "appointed_on");

        Sort expected = Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before");

        assertEquals(expected, sortCaptor.getValue());
    }

    @Test
    void testOfficerRoleSortBySurname() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(companyAppointmentRepository.readAllByCompanyNumber(anyString(), sortCaptor.capture()))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", "surname");

        Sort expected = Sort.by(Sort.Direction.ASC, "data.surname", "data.company_name");

        assertEquals(expected, sortCaptor.getValue());
    }

    @Test
    void testOfficerRoleSortByResignedOn() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(companyAppointmentRepository.readAllByCompanyNumber(anyString(), sortCaptor.capture()))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", "resigned_on");

        Sort expected = Sort.by(Sort.Direction.DESC, "data.resigned_on");

        assertEquals(expected, sortCaptor.getValue());
    }

    @Test
    void testOfficerRoleSortByThrowsBadRequestExceptionWhenInvalidParameter() throws Exception{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);

        when(companyAppointmentRepository.readAllByCompanyNumber(anyString(), sortCaptor.capture()))
                .thenReturn(allAppointmentData);

        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter", "wrongparameter");

        Sort expected = Sort.by(Sort.Direction.DESC, "data.resigned_on");

        assertEquals("Invalid order by parameter [%s]", sortCaptor.getValue());
        
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
