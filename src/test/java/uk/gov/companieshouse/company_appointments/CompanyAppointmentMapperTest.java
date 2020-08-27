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
    void testCompanyAppointmentMapper() {
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
                        .build())
                .withIdentification(IdentificationData.builder()
                        .withIdentificationType("Identification type")
                        .withLegalAuthority("Legal authority")
                        .withLegalForm("Legal form")
                        .withPlaceRegistered("Place registered")
                        .withRegistrationNumber("Registration number")
                        .build())
                .withFormerNames(Collections.singletonList(new FormerNamesData("Forename", "Surname")));
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

    private CompanyAppointmentView.Builder expectedCompanyAppointment() {
        return CompanyAppointmentView.builder()
                .withAppointedOn(LocalDateTime.of(2020, 8, 26, 12, 0))
                .withResignedOn(LocalDateTime.of(2020, 8, 26, 13, 0))
                .withCountryOfResidence("Country")
                .withDateOfBirth(new DateOfBirth(1, 1, 1980))
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
                        .build())
                .withIdentification(IdentificationView.builder()
                        .withIdentificationType("Identification type")
                        .withLegalAuthority("Legal authority")
                        .withLegalForm("Legal form")
                        .withPlaceRegistered("Place registered")
                        .withRegistrationNumber("Registration number")
                        .build())
                .withFormerNames(Collections.singletonList(new FormerNamesView("Forename", "Surname")));
    }

    private CompanyAppointmentView personalAppointmentViewWithOtherForenames(){
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename Other-Forename")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoOtherForenames(){
        return expectedCompanyAppointment()
                .withName("SURNAME, Forename")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoForenamesOrOtherForenames(){
        return expectedCompanyAppointment()
                .withName("SURNAME")
                .build();
    }

    private CompanyAppointmentView personalAppointmentViewWithNoForenamesAndOtherForenames(){
        return expectedCompanyAppointment()
                .withName("SURNAME, Other-Forename")
                .build();
    }


}
