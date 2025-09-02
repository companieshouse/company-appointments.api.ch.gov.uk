package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;
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
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData().forename("Forename")
                        .otherForenames("Other-Forename")
                        .surname("SURNAME")
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename Other-Forename"), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData().forename("Forename")
                        .surname("SURNAME")
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename"), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoForenameOrOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .surname("SURNAME")
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME"), actual);
    }

    @Test
    void testCompanyAppointmentMapperNoForenameAndOtherForenames() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .surname("SURNAME")
                        .otherForenames("Other-Forename")
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Other-Forename"), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithForenamesAndTitle() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData().forename("Forename")
                        .otherForenames("Other-Forename")
                        .surname("SURNAME")
                        .title("Dr")
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename Other-Forename, Dr"), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithForenamesOmitsTitle() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .surname("SURNAME")
                        .title("Mr")
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME"), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithCorporateOfficer() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(companyAppointmentData(officerData()
                .companyName("Company Name")
                .identification(new DeltaIdentification()
                        .setIdentificationType(IDENTIFICATION_TYPE)
                        .setLegalAuthority("Legal authority")
                        .setLegalForm("Legal form")
                        .setPlaceRegistered("Place registered")
                        .setRegistrationNumber("Registration number"))
                .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("Company Name")
                .identification(new CorporateIdent()
                        .identificationType(CorporateIdent.IdentificationTypeEnum.fromValue(IDENTIFICATION_TYPE))
                        .legalAuthority("Legal authority")
                        .legalForm("Legal form")
                        .placeRegistered("Place registered")
                        .registrationNumber("Registration number")), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithFormerName() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .forename("Forename")
                        .surname("SURNAME")
                        .formerNames(Collections.singletonList(new DeltaFormerNames()
                                .setForenames("Forename")
                                .setSurname("Surname")))
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                        .name("SURNAME, Forename")
                        .formerNames(Collections.singletonList(new FormerNames().forenames("Forename").surname("Surname"))),
                actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutServiceAddress() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .forename("Forename")
                        .surname("SURNAME")
                        .serviceAddress(null)
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .address(null), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutLinks() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .forename("Forename")
                        .surname("SURNAME")
                        .links(null)
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .links(null), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutAppointmentLink() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .forename("Forename")
                        .surname("SURNAME")
                        .links(new DeltaItemLinkTypes())
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .links(new ItemLinkTypes().self(null).officer(new OfficerLinkTypes())), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutDateOfBirth() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .forename("Forename")
                        .surname("SURNAME")
                        .build())
                        .sensitiveData(new DeltaSensitiveData()));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .dateOfBirth(null), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithoutSensitiveData() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
                        .forename("Forename")
                        .surname("SURNAME")
                        .build())
                        .sensitiveData(null));

        //then
        assertEquals(expectedCompanyAppointment()
                .name("SURNAME, Forename")
                .dateOfBirth(null), actual);
    }

    @Test
    void testCompanyAppointmentMapperDoesNotMapCountryOfResidenceOrDOBForSecretarialRoles() {
        SecretarialRoles.stream().forEach(secretary -> {
            //when
            OfficerSummary actual = companyAppointmentMapper.map(
                    companyAppointmentData(officerData()
                            .officerRole(secretary.getRole())
                            .build()));

            //then
            assertEquals(expectedCompanyAppointment()
                    .officerRole(OfficerSummary.OfficerRoleEnum.fromValue(secretary.getRole()))
                    .countryOfResidence(null)
                    .dateOfBirth(null), actual);
        });
    }

    @Test
    void testCompanyAppointmentMapperWithPrincipalOfficeAddress() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData()
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
                        .build()));

        //then
        assertEquals(expectedCompanyAppointment()
                .principalOfficeAddress(new PrincipalOfficeAddress()
                        .addressLine1("Address 1")
                        .addressLine2("Address 2")
                        .careOf("Care of")
                        .country("Country")
                        .locality("Locality")
                        .postalCode("AB01 9XY")
                        .poBox("PO Box")
                        .premises("Premises")
                        .region("Region")), actual);
    }

    @Test
    void testCompanyAppointmentMapperWithRegisterViewTrue() {
        //when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(officerData().build()), true);
        //then
        assertEquals(expectedCompanyAppointment()
                .dateOfBirth(new DateOfBirth().day(null).month(1).year(1980)), actual);
    }

    @Test
    void testPre1992Appointment() {
        // when
        OfficerSummary actual = companyAppointmentMapper.map(
                companyAppointmentData(pre1992AppointmentData()));

        // then
        assertEquals(pre1992AppointmentSummary(), actual);
    }

    private CompanyAppointmentDocument companyAppointmentData(DeltaOfficerData officerData) {
        return new CompanyAppointmentDocument()
                .id("123")
                .data(officerData)
                .sensitiveData(new DeltaSensitiveData().setDateOfBirth(
                        LocalDateTime.of(1980, 1, 1, 12, 0).toInstant(ZoneOffset.UTC)))
                .companyStatus("active");
    }

    private DeltaOfficerData pre1992AppointmentData() {
        return DeltaOfficerData.Builder.builder()
                .isPre1992Appointment(true)
                .appointedBefore(LocalDateTime.of(1991, 11, 10, 0, 0)
                        .toInstant(ZoneOffset.UTC))
                .resignedOn(LocalDateTime.of(2020, 8, 26, 13, 0)
                        .toInstant(ZoneOffset.UTC))
                .countryOfResidence("Country")
                .links(new DeltaItemLinkTypes()
                        .setSelf("/company/12345678/appointment/123")
                        .setOfficer(new DeltaOfficerLinkTypes()
                                .setSelf("/officers/abc")
                                .setAppointments("/officers/abc/appointments")))
                .nationality("Nationality")
                .occupation("Occupation")
                .officerRole(OfficerSummary.OfficerRoleEnum.DIRECTOR.toString())
                .etag("ETAG")
                .serviceAddress(new DeltaServiceAddress()
                        .setAddressLine1("Address 1")
                        .setCountry("Country")
                        .setLocality("Locality"))
                .build();
    }

    private OfficerSummary pre1992AppointmentSummary() {
        return new OfficerSummary()
                .isPre1992Appointment(true)
                .appointedBefore(LocalDate.of(1991, 11, 10))
                .resignedOn(LocalDate.of(2020, 8, 26))
                .countryOfResidence("Country")
                .dateOfBirth(new DateOfBirth().year(1980).month(1))
                .links(new ItemLinkTypes()
                        .self("/company/12345678/appointment/123")
                        .officer(new OfficerLinkTypes()
                                .appointments("/officers/abc/appointments")))
                .nationality("Nationality")
                .occupation("Occupation")
                .officerRole(OfficerSummary.OfficerRoleEnum.DIRECTOR)
                .address(new Address()
                        .addressLine1("Address 1")
                        .country("Country")
                        .locality("Locality"));
    }

    private DeltaOfficerData.Builder officerData() {
        return DeltaOfficerData.Builder.builder()
                .appointedOn(LocalDateTime.of(2020, 8, 26, 12, 0)
                        .toInstant(ZoneOffset.UTC))
                .resignedOn(LocalDateTime.of(2020, 8, 26, 13, 0)
                        .toInstant(ZoneOffset.UTC))
                .countryOfResidence("Country")
                .links(new DeltaItemLinkTypes()
                        .setSelf("/company/12345678/appointment/123")
                        .setOfficer(new DeltaOfficerLinkTypes()
                                .setSelf("/officers/abc")
                                .setAppointments("/officers/abc/appointments")))
                .nationality("Nationality")
                .occupation("Occupation")
                .officerRole(OfficerSummary.OfficerRoleEnum.MANAGING_OFFICER.toString())
                .etag("ETAG")
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
                .contactDetails(new DeltaContactDetails().setContactName("Name"))
                .personNumber("personNumber");
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
                .contactDetails(new ContactDetails()
                        .contactName("Name"))
                .personNumber("personNumber");
    }
}
