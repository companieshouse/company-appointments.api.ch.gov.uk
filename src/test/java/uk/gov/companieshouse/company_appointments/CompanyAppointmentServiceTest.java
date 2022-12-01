package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void testFetchAppointmentForCompanyNumberForNotResignedReturnsMappedAppointmentData() throws NotFoundException{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);


        when(companyAppointmentRepository.readAllByCompanyNumberForNotResigned(COMPANY_NUMBER))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, filter);

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumberForNotResigned(COMPANY_NUMBER);
    }

    @Test
    void testFetchAppointmentForCompanyThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readAllByCompanyNumber(any()))
                .thenReturn(new ArrayList<>());

        Executable result = () -> companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter");

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testFetchAppointmentForCompanyNumberReturnsMappedAppointmentData() throws NotFoundException{
        CompanyAppointmentData officerData = new CompanyAppointmentData("1", officerData().build());

        List<CompanyAppointmentData> allAppointmentData = new ArrayList<>();
        allAppointmentData.add(officerData);


        when(companyAppointmentRepository.readAllByCompanyNumber(COMPANY_NUMBER))
                .thenReturn(allAppointmentData);


        AllCompanyAppointmentsView result = companyAppointmentService.fetchAppointmentsForCompany(COMPANY_NUMBER, "filter");

        assertEquals(1, result.getTotalResults());
        assertEquals(AllCompanyAppointmentsView.class, result.getClass());
        verify(companyAppointmentRepository).readAllByCompanyNumber(COMPANY_NUMBER);
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
