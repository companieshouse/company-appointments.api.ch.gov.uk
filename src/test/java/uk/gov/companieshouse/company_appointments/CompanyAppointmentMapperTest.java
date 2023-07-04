package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.Address;
import uk.gov.companieshouse.api.appointment.ContactDetails;
import uk.gov.companieshouse.api.appointment.CorporateIdent;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;

class CompanyAppointmentMapperTest {

    public static final String IDENTIFICATION_TYPE = "eea";
    private CompanyAppointmentMapper companyAppointmentMapper;

    @BeforeEach
    void setUp() {
        companyAppointmentMapper = new CompanyAppointmentMapper();
    }

    @Test
    void testCompanyAppointmentMapperForenameAndOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithNoOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoForenameOrOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoForenameOrOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithNoForenamesOrOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoForenameAndOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoForenameAndOtherForenames()));

        //then
        assertEquals(personalAppointmentViewWithNoForenamesAndOtherForenames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithForenamesAndTitle() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithForenamesAndTitle()));

        //then
        assertEquals(personalAppointmentViewWithForenamesAndTitle(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithForenamesOmitsTitle() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithNoForenamesOmitsTitle()));

        //then
        assertEquals(personalAppointmentViewWithNoForenamesOmitsTitle(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithCorporateOfficer() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(corporateAppointmentData()));

        //then
        assertEquals(corporateAppointmentView(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithFormerName() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithFormerNames()));

        //then
        assertEquals(personalAppointmentViewWithFormerNames(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutServiceAddress() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutServiceAddress()));

        //then
        assertEquals(personalAppointmentViewWithoutServiceAddress(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutLinks() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutLinks()));

        //then
        assertEquals(personalAppointmentViewWithoutLinks(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutAppointmentLink() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutAppointmentLink()));

        //then
        assertEquals(personalAppointmentViewWithoutAppointmentLink(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutDateOfBirth() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithoutDateOfBirth()));

        //then
        assertEquals(personalAppointmentViewWithoutDateOfBirth(), actual);
    }

    @Test
    void testCompanyAppointmentMapperDoesNotMapCountryOfResidenceOrDOBForSecretarialRoles(){
        SecretarialRoles.stream().forEach(s -> {
            //when
            OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataForSecretarialRole(s)));

            //then
            assertEquals(personalAppointmentViewOmitCountryOfResidenceAndDOBForSecretarialRole(s), actual);
        });
    }

    @Test
    void testCompanyAppointmentMapperWithResponsibilities() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithResponsibilities()));

        //then
        assertEquals(personalAppointmentViewResponsibilities(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithPrincipalOfficeAddress() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithPrincipalOfficeAddress()));

        //then
        assertEquals(personalAppointmentViewPrincipalOfficeAddress(), actual);
    }

    @Test
    void testCompanyAppointmentMapperContactDetails() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithContactDetails()));

        //then
        assertEquals(personalAppointmentViewWithContactDetails(), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithRegisterViewTrue(){
        //when
        companyAppointmentMapper.setRegisterView(true);
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(personalAppointmentDataWithFullDateOfBirth()));
        //then
        assertEquals(personalAppointmentViewWithFullDateOfBirth(), actual);
    }

    private CompanyAppointmentData companyAppointmentData(OfficerData officerData) {
        return new CompanyAppointmentData("123", officerData, "active");
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
                .withOfficerRole(OfficerSummary.OfficerRoleEnum.MANAGING_OFFICER.toString())
                .withEtag("ETAG")
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
                .withIdentificationType(IDENTIFICATION_TYPE)
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

    private OfficerData personalAppointmentDataForSecretarialRole(SecretarialRoles secretary){
        return officerData()
                .withOfficerRole(secretary.getRole())
                .build();
    }

    private OfficerData personalAppointmentDataWithResponsibilities() {
        return officerData()
                .withResponsibilities("responsibilities")
                .build();
    }

    private OfficerData personalAppointmentDataWithPrincipalOfficeAddress() {
        return officerData()
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
                .build();
    }

    private OfficerData personalAppointmentDataWithContactDetails() {
        return officerData()
                .withContactDetails(ContactDetailsData.builder()
                        .withContactName("John")
                        .build())
                .build();
    }

    private OfficerSummary expectedCompanyAppointment() {
        return new OfficerSummary()
                .appointedOn(LocalDate.of(2020, 8, 26))
                .resignedOn(LocalDate.of(2020, 8, 26))
                .countryOfResidence("Country")
                .dateOfBirth(new DateOfBirth()
                        .month(1)
                        .year(1980))
                .links(new ItemLinkTypes()
                        .self("/company/12345678/appointment/123")
                        .officer(new OfficerLinkTypes()
                                .self(null)
                                .appointments("/officers/abc/appointments")))
                .nationality("Nationality")
                .occupation("Occupation")
                .officerRole(OfficerSummary.OfficerRoleEnum.MANAGING_OFFICER)
                .etag(null)
                .address(new Address()
                        .addressLine1("Address 1")
                        .addressLine2("Address 2")
                        .careOf("Care of")
                        .country("Country")
                        .locality("Locality")
                        .postalCode("AB01 9XY")
                        .poBox("PO Box")
                        .premises("Premises")
                        .region("Region"))
                .responsibilities("responsibilities")
                .principalOfficeAddress(new PrincipalOfficeAddress()
                        .addressLine1("Address 1")
                        .addressLine2("Address 2")
                        .careOf("Care of")
                        .country("Country")
                        .locality("Locality")
                        .postalCode("AB01 9XY")
                        .poBox("PO Box")
                        .premises("Premises")
                        .region("Region"))
                .contactDetails(new ContactDetails()
                        .contactName("Name"));
    }


    private OfficerSummary personalAppointmentViewWithFullDateOfBirth() {
        return expectedCompanyAppointment()
                .dateOfBirth(new DateOfBirth().day(1).month(1).year(1980));
    }

    private OfficerData personalAppointmentDataWithFullDateOfBirth() {
        return officerData()
                .withDateOfBirth(LocalDateTime.of(1980,1, 1, 0, 12))
                .build();
    }

    private OfficerSummary personalAppointmentViewWithOtherForenames() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename Other-Forename");
    }

    private OfficerSummary personalAppointmentViewWithNoOtherForenames() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename");
    }

    private OfficerSummary personalAppointmentViewWithNoForenamesOrOtherForenames() {
        return expectedCompanyAppointment()
                .name("SURNAME");
    }

    private OfficerSummary personalAppointmentViewWithNoForenamesAndOtherForenames() {
        return expectedCompanyAppointment()
                .name("SURNAME, Other-Forename");
    }

    private OfficerSummary personalAppointmentViewWithForenamesAndTitle() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename Other-Forename, Dr");
    }

    private OfficerSummary personalAppointmentViewWithNoForenamesOmitsTitle() {
        return expectedCompanyAppointment()
                .name("SURNAME");
    }

    private OfficerSummary corporateAppointmentView() {
        return expectedCompanyAppointment()
                .name("Company Name")
                .identification(new CorporateIdent()
                    .identificationType(CorporateIdent.IdentificationTypeEnum.fromValue(IDENTIFICATION_TYPE))
                .legalAuthority("Legal authority")
                .legalForm("Legal form")
                .placeRegistered("Place registered")
                .registrationNumber("Registration number"));
    }

    private OfficerSummary personalAppointmentViewWithFormerNames() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .formerNames(Collections.singletonList(new FormerNames().forenames("Forename").surname("Surname")));
    }

    private OfficerSummary personalAppointmentViewWithoutServiceAddress() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .address(null);
    }

    private OfficerSummary personalAppointmentViewWithoutLinks() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .links(null);
    }

    private OfficerSummary personalAppointmentViewWithoutAppointmentLink() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .links(new ItemLinkTypes().self(null).officer(new OfficerLinkTypes()));
    }

    private OfficerSummary personalAppointmentViewWithoutDateOfBirth() {
        return expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .dateOfBirth(null);
    }

    private OfficerSummary personalAppointmentViewOmitCountryOfResidenceAndDOBForSecretarialRole(SecretarialRoles secretary){
        return expectedCompanyAppointment()
                .officerRole(OfficerSummary.OfficerRoleEnum.fromValue(secretary.getRole()))
                .countryOfResidence(null)
                .dateOfBirth(null);
    }

    private OfficerSummary personalAppointmentViewResponsibilities() {
        return expectedCompanyAppointment()
                .responsibilities("responsibilities");
    }

    private OfficerSummary personalAppointmentViewPrincipalOfficeAddress() {
        return expectedCompanyAppointment()
                .address(new Address()
                        .addressLine1("Address 1")
                        .addressLine2("Address 2")
                        .careOf("Care of")
                        .country("Country")
                        .locality("Locality")
                        .postalCode("AB01 9XY")
                        .poBox("PO Box")
                        .premises("Premises")
                        .region("Region"));
    }

    private OfficerSummary personalAppointmentViewWithContactDetails() {
        return expectedCompanyAppointment()
                .contactDetails(new ContactDetails()
                        .contactName("John"));
    }
}
