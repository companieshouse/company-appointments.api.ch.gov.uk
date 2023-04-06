package uk.gov.companieshouse.company_appointments.model.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaDateOfBirth;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaUsualResidentialAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordViewTest {

    private static final LocalDate INSTANT_ONE = LocalDate.parse("2009-01-12");
    private static final LocalDate INSTANT_TWO = LocalDate.parse("2019-01-12");
    private static final LocalDate INSTANT_THREE = LocalDate.parse("1991-01-12");


    private List<DeltaFormerNames> formerNames
            = buildFormerNamesList("John", "Davies");
    private DeltaIdentification identification = buildIdentification();
    private DeltaDateOfBirth INSTANT_DOB = buildDateOfBirth(12,1,1989);
    private DeltaItemLinkTypes links = buildLinksItem();
    private DeltaContactDetails contactDetails = buildContactDetails();
    private DateOfBirthView dob = new DateOfBirthView(12,1,1989);

    private CompanyAppointmentFullRecordView testView;

    @BeforeEach
    void setUp() {

        CompanyAppointmentDocument companyAppointmentDocument = new CompanyAppointmentDocument();
        companyAppointmentDocument.setData(new DeltaOfficerData());
        companyAppointmentDocument.setSensitiveData(new DeltaSensitiveData());

        companyAppointmentDocument.getData().setServiceAddress(createServiceAddress("service"));
        companyAppointmentDocument.getSensitiveData().setUsualResidentialAddress(createUsualResidentialAddress("usualResidential"));
        companyAppointmentDocument.getData().setAppointedOn(INSTANT_ONE);
        companyAppointmentDocument.getData().setAppointedBefore(INSTANT_THREE);
        companyAppointmentDocument.getData().setCountryOfResidence("countryOfResidence");
        companyAppointmentDocument.getSensitiveData().setDateOfBirth(INSTANT_DOB);
        companyAppointmentDocument.getData().setFormerNames(formerNames);;
        companyAppointmentDocument.getData().setIdentification(identification);
        companyAppointmentDocument.getData().setLinks(buildLinksItem());
        companyAppointmentDocument.getData().setSurname("Davies");
        companyAppointmentDocument.getData().setForename("James");
        companyAppointmentDocument.getData().setTitle("Sir");
        companyAppointmentDocument.getData().setNationality("Welsh");
        companyAppointmentDocument.getData().setOccupation("occupation");
        companyAppointmentDocument.getData().setOfficerRole(DeltaOfficerData.OfficerRoleEnum.DIRECTOR);
        companyAppointmentDocument.getData().setResignedOn(INSTANT_TWO);
        companyAppointmentDocument.setEtag("etag");
        companyAppointmentDocument.getData().setPersonNumber("1234");
        companyAppointmentDocument.getData().setPre1992Appointment(Boolean.TRUE);
        companyAppointmentDocument.getData().setContactDetails(contactDetails);
        companyAppointmentDocument.getData().setResponsibilities("responsibilities");
        companyAppointmentDocument.getData().setPrincipalOfficeAddress(createPrincipalOfficeAddress("principleOffice"));

        testView = CompanyAppointmentFullRecordView.Builder.view(companyAppointmentDocument).build();
    }

    @Test
    void serviceAddress() {

        checkServiceAddress(testView.getServiceAddress(), "service");
    }

    @Test
    void usualResidentialAddress() {

        checkUsualResidentialAddress(testView.getUsualResidentialAddress(), "usualResidential");
    }

    @Test
    void principleOfficeAddress() {

        checkPrincipleOfficeAddress(testView.getPrincipalOfficeAddress(), "principleOffice");
    }

    private void checkUsualResidentialAddress(DeltaUsualResidentialAddress address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", prefix, "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", prefix, "address2")));
        assertThat(address.getCareOf(), is(String.join(" ", prefix, "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", prefix, "country")));
        assertThat(address.getLocality(), is(String.join(" ", prefix, "locality")));
        assertThat(address.getPoBox(), is(String.join(" ", prefix, "poBox")));
        assertThat(address.getPostalCode(), is(String.join(" ", prefix, "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", prefix, "premises")));
        assertThat(address.getRegion(), is(String.join(" ", prefix, "region")));
    }

    private void checkServiceAddress(DeltaServiceAddress address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", prefix, "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", prefix, "address2")));
        assertThat(address.getCountry(), is(String.join(" ", prefix, "country")));
        assertThat(address.getLocality(), is(String.join(" ", prefix, "locality")));
        assertThat(address.getPostalCode(), is(String.join(" ", prefix, "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", prefix, "premises")));
        assertThat(address.getRegion(), is(String.join(" ", prefix, "region")));
    }

    private void checkPrincipleOfficeAddress(DeltaPrincipalOfficeAddress address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", prefix, "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", prefix, "address2")));
        assertThat(address.getCareOf(), is(String.join(" ", prefix, "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", prefix, "country")));
        assertThat(address.getLocality(), is(String.join(" ", prefix, "locality")));
        assertThat(address.getPoBox(), is(String.join(" ", prefix, "poBox")));
        assertThat(address.getPostalCode(), is(String.join(" ", prefix, "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", prefix, "premises")));
        assertThat(address.getRegion(), is(String.join(" ", prefix, "region")));
    }

    @Test
    void appointedOn() {

        assertThat(testView.getAppointedOn(), is(INSTANT_ONE));
    }

    @Test
    void appointedBefore() {

        assertThat(testView.getAppointedBefore(), is(INSTANT_THREE));
    }

    @Test
    void countryOfResidence() {

        assertThat(testView.getCountryOfResidence(), is("countryOfResidence"));
    }

    @Test
    void DateOfBirth() {

        assertThat(testView.getDateOfBirth(), is(dob));
    }

    @Test
    void FormerNames() {

        assertThat(testView.getFormerNames(), is(formerNames));
    }

    @Test
    void Identification() {

        assertThat(testView.getIdentification(), is(identification));
    }

    @Test
    void Links() {

        assertThat(testView.getLinks(), is(links));
    }

    @Test
    void Name() {

        assertThat(testView.getName(), is("Davies, James, Sir"));
    }

    @Test
    void Nationality() {

        assertThat(testView.getNationality(), is("Welsh"));
    }

    @Test
    void Occupation() {

        assertThat(testView.getOccupation(), is("occupation"));
    }

    @Test
    void OfficerRole() {

        assertThat(testView.getOfficerRole(), is("director"));
    }

    @Test
    void ResignedOn() {

        assertThat(testView.getResignedOn(), is(INSTANT_TWO));
    }

    @Test
    void etag() {

        assertThat(testView.getEtag(), is("etag"));
    }

    @Test
    void personNumber() {

        assertThat(testView.getPersonNumber(), is("1234"));
    }

    @Test
    void isPre1998Appointment() {

        assertThat(testView.getIsPre1992Appointment(), is(Boolean.TRUE));
    }

    @Test
    void contactDetails() {

        assertThat(testView.getContactDetails(), is(contactDetails));
    }

    @Test
    void responsibilities() {

        assertThat(testView.getResponsibilities(), is("responsibilities"));
    }

    private DeltaServiceAddress createServiceAddress(String prefix) {

        DeltaServiceAddress address = new DeltaServiceAddress();
        address.setAddressLine1(String.join(" ", prefix, "address1"));
        address.setAddressLine2(String.join(" ", prefix, "address2"));
        address.setCountry(String.join(" ", prefix, "country"));
        address.setLocality(String.join(" ", prefix, "locality"));
        address.setPostalCode(String.join(" ", prefix, "postcode"));
        address.setPremises(String.join(" ", prefix, "premises"));
        address.setRegion(String.join(" ", prefix, "region"));

        return address;
    }

    private DeltaUsualResidentialAddress createUsualResidentialAddress(String prefix) {

        DeltaUsualResidentialAddress address = new DeltaUsualResidentialAddress();
        address.setAddressLine1(String.join(" ", prefix, "address1"));
        address.setAddressLine2(String.join(" ", prefix, "address2"));
        address.setCareOf(String.join(" ", prefix, "careOf"));
        address.setCountry(String.join(" ", prefix, "country"));
        address.setLocality(String.join(" ", prefix, "locality"));
        address.setPoBox(String.join(" ", prefix, "poBox"));
        address.setPostalCode(String.join(" ", prefix, "postcode"));
        address.setPremises(String.join(" ", prefix, "premises"));
        address.setRegion(String.join(" ", prefix, "region"));

        return address;
    }

    private DeltaPrincipalOfficeAddress createPrincipalOfficeAddress(String prefix) {

        DeltaPrincipalOfficeAddress address = new DeltaPrincipalOfficeAddress();
        address.setAddressLine1(String.join(" ", prefix, "address1"));
        address.setAddressLine2(String.join(" ", prefix, "address2"));
        address.setCareOf(String.join(" ", prefix, "careOf"));
        address.setCountry(String.join(" ", prefix, "country"));
        address.setLocality(String.join(" ", prefix, "locality"));
        address.setPoBox(String.join(" ", prefix, "poBox"));
        address.setPostalCode(String.join(" ", prefix, "postcode"));
        address.setPremises(String.join(" ", prefix, "premises"));
        address.setRegion(String.join(" ", prefix, "region"));

        return address;
    }

    private List<DeltaFormerNames> buildFormerNamesList(String forename, String surname) {
        DeltaFormerNames formerNamesItems = new DeltaFormerNames();
        formerNamesItems.setForenames(forename);
        formerNamesItems.setSurname(surname);
        return Collections.singletonList(formerNamesItems);
    }

    private DeltaDateOfBirth buildDateOfBirth(int day, int month, int year) {
        DeltaDateOfBirth dob = new DeltaDateOfBirth();
        dob.setDay(day);
        dob.setMonth(month);
        dob.setYear(year);
        return dob;
    }

    private DeltaIdentification buildIdentification() {
        DeltaIdentification identification = new DeltaIdentification();
        identification.setIdentificationType(DeltaIdentification.IdentificationTypeEnum.EEA);
        identification.setLegalAuthority("Chapter 32");
        identification.setLegalForm("Hong Kong");
        identification.setPlaceRegistered("UK");
        identification.setRegistrationNumber("32982");
        return identification;
    }

    private DeltaItemLinkTypes buildLinksItem() {
        DeltaItemLinkTypes linksItem = new DeltaItemLinkTypes();
        linksItem.setSelf("/officers/abcde123456789/full_record");
        DeltaOfficerLinkTypes officerLinkTypes = new DeltaOfficerLinkTypes();
        officerLinkTypes.setAppointments("/company/01777777/appointments/123456789abcde");
        linksItem.setOfficer(officerLinkTypes);
        return linksItem;
    }

    private DeltaContactDetails buildContactDetails() {
        DeltaContactDetails contactDetails = new DeltaContactDetails();
        contactDetails.setContactName("John Smith");
        return contactDetails;
    }
}
