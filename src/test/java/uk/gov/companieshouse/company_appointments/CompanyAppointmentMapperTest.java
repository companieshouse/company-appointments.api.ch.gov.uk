package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirth;
import uk.gov.companieshouse.company_appointments.model.view.FormerNamesView;
import uk.gov.companieshouse.company_appointments.model.view.IdentificationView;
import uk.gov.companieshouse.company_appointments.model.view.LinksView;
import uk.gov.companieshouse.company_appointments.model.view.ServiceAddressView;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompanyAppointmentMapperTest {

    private CompanyAppointmentMapper companyAppointmentMapper;

    @BeforeEach
    void setUp() {
        companyAppointmentMapper = new CompanyAppointmentMapper();
    }

    @Test
    void testCompanyAppointmentMapperForenameAndOtherForenames() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoOtherForenames() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithNoOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoForenameOrOtherForenames() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoForenameOrOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithNoForenamesOrOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoForenameAndOtherForenames() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoForenameAndOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithNoForenamesAndOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithForenamesAndTitle() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithForenamesAndTitle()));

        //then
        assertEquals(personalAppointmentViewWithForenamesAndTitle(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithForenamesOmitsTitle() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoForenamesOmitsTitle()));

        //then
        assertEquals(personalAppointmentViewWithNoForenamesOmitsTitle(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithCorporateOfficer() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(corporateAppointmentData()));

        //then
        assertEquals(corporateAppointmentView(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithFormerName() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithFormerNames()));

        //then
        assertEquals(personalAppointmentViewWithFormerNames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutServiceAddress() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutServiceAddress()));

        //then
        assertEquals(personalAppointmentViewWithoutServiceAddress(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutLinks() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutLinks()));

        //then
        assertEquals(personalAppointmentViewWithoutLinks(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutAppointmentLink() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutAppointmentLink()));

        //then
        assertEquals(personalAppointmentViewWithoutAppointmentLink(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutDateOfBirth() {
        //when
        CompanyAppointmentView actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutDateOfBirth()));

        //then
        assertEquals(personalAppointmentViewWithoutDateOfBirth(), actual);
    }

    private CompanyAppointmentData companyAppointmentData(OfficerData officerData) {
        return new CompanyAppointmentData("123", officerData);
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
                        .build());
    }

    private OfficerData personalAppointmentDataWithOtherForenames() {
        return officerData().withForename("Forename")
                .withOtherForenames("Other-Forename")
                .withSurname("SURNAME")
                .build();
    }

    private OfficerData personalAppointmentDataWithNoOtherForenames() {
        return officerData().withForename("Forename")
                .withSurname("SURNAME")
                .build();
    }

    private OfficerData personalAppointmentDataWithNoForenameOrOtherForenames() {
        return officerData()
                .withSurname("SURNAME")
                .build();
    }

    private OfficerData personalAppointmentDataWithNoForenameAndOtherForenames() {
        return officerData()
                .withSurname("SURNAME")
                .withOtherForenames("Other-Forename")
                .build();
    }

    private OfficerData personalAppointmentDataWithForenamesAndTitle() {
        return officerData().withForename("Forename")
                .withOtherForenames("Other-Forename")
                .withSurname("SURNAME")
                .withTitle("Dr")
                .build();
    }

    private OfficerData personalAppointmentDataWithNoForenamesOmitsTitle() {
        return officerData()
                .withSurname("SURNAME")
                .withTitle("Mr")
                .build();
    }

    private OfficerData corporateAppointmentData() {
        return officerData()
                .withCompanyName("Company Name")
                .withIdentification(IdentificationData.builder()
                .withIdentificationType("Identification type")
                .withLegalAuthority("Legal authority")
                .withLegalForm("Legal form")
                .withPlaceRegistered("Place registered")
                .withRegistrationNumber("Registration number")
                .build())
                .build();
    }

    private OfficerData personalAppointmentDataWithFormerNames() {
        return officerData()
                .withForename("Forename")
                .withSurname("SURNAME")
                .withFormerNames(Collections.singletonList(new FormerNamesData("Forename", "Surname")))
                .build();
    }

    private OfficerData personalAppointmentDataWithoutServiceAddress() {
        return officerData()
                .withForename("Forename")
                .withSurname("SURNAME")
                .withServiceAddress(null)
                .build();
    }

    private OfficerData personalAppointmentDataWithoutLinks() {
        return officerData()
                .withForename("Forename")
                .withSurname("SURNAME")
                .withLinks(null)
                .build();
    }

    private OfficerData personalAppointmentDataWithoutAppointmentLink() {
        return officerData()
                .withForename("Forename")
                .withSurname("SURNAME")
                .withLinks(new LinksData(null, null))
                .build();
    }

    private OfficerData personalAppointmentDataWithoutDateOfBirth() {
        return officerData()
                .withForename("Forename")
                .withSurname("SURNAME")
                .withDateOfBirth(null)
                .build();
    }

    private CompanyAppointmentView.Builder expectedCompanyAppointment() {
        return CompanyAppointmentView.builder()
                .withAppointedOn(LocalDateTime.of(2020, 8, 26, 12, 0))
                .withResignedOn(LocalDateTime.of(2020, 8, 26, 13, 0))
                .withCountryOfResidence("Country")
                .withDateOfBirth(new DateOfBirth(null, 1, 1980))
                .withLinks(new LinksView("/company/12345678/appointment/123", "/officers/abc/appointments"))
                .withNationality("Nationality")
                .withOccupation("Occupation")
                .withOfficerRole("Role")
                .withServiceAddress(ServiceAddressView.builder()
                        .withAddressLine1("Address 1")
                        .withAddressLine2("Address 2")
                        .withCareOf("Care of")
                        .withCountry("Country")
                        .withLocality("Locality")
                        .withPostcode("AB01 9XY")
                        .withPoBox("PO Box")
                        .withPremises("Premises")
                        .withRegion("Region")
                        .build());
    }

    private CompanyAppointmentView personalAppointmentViewWithOtherForenames() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename Other-Forename")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoOtherForenames() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoForenamesOrOtherForenames() {
        return expectedCompanyAppointment()
                .withName("SURNAME")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoForenamesAndOtherForenames() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Other-Forename")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithForenamesAndTitle() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename Other-Forename, Dr")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoForenamesOmitsTitle() {
        return expectedCompanyAppointment()
                .withName("SURNAME")
                .build();
    }

    private CompanyAppointmentView corporateAppointmentView() {
        return expectedCompanyAppointment()
                .withName("Company Name")
                .withIdentification(IdentificationView.builder()
                .withIdentificationType("Identification type")
                .withLegalAuthority("Legal authority")
                .withLegalForm("Legal form")
                .withPlaceRegistered("Place registered")
                .withRegistrationNumber("Registration number")
                .build())
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithFormerNames() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .withFormerNames(Collections.singletonList(new FormerNamesView("Forename", "Surname")))
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithoutServiceAddress() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .withServiceAddress(null)
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithoutLinks() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .withLinks(null)
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithoutAppointmentLink() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .withLinks(new LinksView(null, (String)null))
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithoutDateOfBirth() {
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .withDateOfBirth(null)
                .build();
    }
}
