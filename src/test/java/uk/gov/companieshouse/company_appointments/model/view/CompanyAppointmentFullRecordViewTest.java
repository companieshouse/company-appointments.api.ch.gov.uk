package uk.gov.companieshouse.company_appointments.model.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.api.appointment.ContactDetails;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordViewTest {

    private static final LocalDate INSTANT_ONE = LocalDate.parse("2009-01-12");
    private static final LocalDate INSTANT_TWO = LocalDate.parse("2019-01-12");
    private static final LocalDate INSTANT_THREE = LocalDate.parse("1991-01-12");


    private List<FormerNames> formerNames
            = buildFormerNamesList("John", "Davies");
    private Identification identification = buildIdentification();
    private DateOfBirth INSTANT_DOB = buildDateOfBirth(12,1,1989);
    private ItemLinkTypes links = buildLinksItem();
    private ContactDetails contactDetails = buildContactDetails();
    private DateOfBirthView dob = new DateOfBirthView(12,1,1989);

    private CompanyAppointmentFullRecordView testView;

    @BeforeEach
    void setUp() {

        DeltaAppointmentApi deltaAppointmentApi = new DeltaAppointmentApi();
        deltaAppointmentApi.setData(new Data());
        deltaAppointmentApi.setSensitiveData(new SensitiveData());

        deltaAppointmentApi.getData().setServiceAddress(createServiceAddress("service"));
        deltaAppointmentApi.getSensitiveData().setUsualResidentialAddress(createUsualResidentialAddress("usualResidential"));
        deltaAppointmentApi.getData().setAppointedOn(INSTANT_ONE);
        deltaAppointmentApi.getData().setAppointedBefore(INSTANT_THREE);
        deltaAppointmentApi.getData().setCountryOfResidence("countryOfResidence");
        deltaAppointmentApi.getSensitiveData().setDateOfBirth(INSTANT_DOB);
        deltaAppointmentApi.getData().setFormerNames(formerNames);;
        deltaAppointmentApi.getData().setIdentification(identification);
        deltaAppointmentApi.getData().setLinks(buildLinksList());
        deltaAppointmentApi.getData().setSurname("Davies");
        deltaAppointmentApi.getData().setForename("James");
        deltaAppointmentApi.getData().setTitle("Sir");
        deltaAppointmentApi.getData().setNationality("Welsh");
        deltaAppointmentApi.getData().setOccupation("occupation");
        deltaAppointmentApi.getData().setOfficerRole(Data.OfficerRoleEnum.DIRECTOR);
        deltaAppointmentApi.getData().setResignedOn(INSTANT_TWO);
        deltaAppointmentApi.setEtag("etag");
        deltaAppointmentApi.getData().setPersonNumber("1234");
        deltaAppointmentApi.getData().setIsPre1992Appointment(Boolean.TRUE);
        deltaAppointmentApi.getData().setContactDetails(contactDetails);
        deltaAppointmentApi.getData().setResponsibilities("responsibilities");
        deltaAppointmentApi.getData().setPrincipalOfficeAddress(createPrincipalOfficeAddress("principleOffice"));

        testView = CompanyAppointmentFullRecordView.Builder.view(deltaAppointmentApi).build();
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

    private void checkUsualResidentialAddress(UsualResidentialAddress address, String prefix) {

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

    private void checkServiceAddress(ServiceAddress address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", prefix, "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", prefix, "address2")));
        assertThat(address.getCountry(), is(String.join(" ", prefix, "country")));
        assertThat(address.getLocality(), is(String.join(" ", prefix, "locality")));
        assertThat(address.getPostalCode(), is(String.join(" ", prefix, "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", prefix, "premises")));
        assertThat(address.getRegion(), is(String.join(" ", prefix, "region")));
    }

    private void checkPrincipleOfficeAddress(PrincipalOfficeAddress address, String prefix) {

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

    private ServiceAddress createServiceAddress(String prefix) {

        ServiceAddress address = new ServiceAddress();
        address.setAddressLine1(String.join(" ", prefix, "address1"));
        address.setAddressLine2(String.join(" ", prefix, "address2"));
        address.setCountry(String.join(" ", prefix, "country"));
        address.setLocality(String.join(" ", prefix, "locality"));
        address.setPostalCode(String.join(" ", prefix, "postcode"));
        address.setPremises(String.join(" ", prefix, "premises"));
        address.setRegion(String.join(" ", prefix, "region"));

        return address;
    }

    private UsualResidentialAddress createUsualResidentialAddress(String prefix) {

        UsualResidentialAddress address = new UsualResidentialAddress();
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

    private PrincipalOfficeAddress createPrincipalOfficeAddress(String prefix) {

        PrincipalOfficeAddress address = new PrincipalOfficeAddress();
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

    private List<FormerNames> buildFormerNamesList(String forename, String surname) {
        FormerNames formerNamesItems = new FormerNames();
        formerNamesItems.setForenames(forename);
        formerNamesItems.setSurname(surname);
        return Collections.singletonList(formerNamesItems);
    }

    private DateOfBirth buildDateOfBirth(int day, int month, int year) {
        DateOfBirth dob = new DateOfBirth();
        dob.setDay(day);
        dob.setMonth(month);
        dob.setYear(year);
        return dob;
    }

    private Identification buildIdentification() {
        Identification identification = new Identification();
        identification.setIdentificationType(Identification.IdentificationTypeEnum.EEA);
        identification.setLegalAuthority("Chapter 32");
        identification.setLegalForm("Hong Kong");
        identification.setPlaceRegistered("UK");
        identification.setRegistrationNumber("32982");
        return identification;
    }

    private List<ItemLinkTypes> buildLinksList() {
        ItemLinkTypes linksItem = new ItemLinkTypes();
        linksItem.setSelf("/officers/abcde123456789");
        OfficerLinkTypes officerLinkTypes = new OfficerLinkTypes();
        officerLinkTypes.setSelf("/officers/abcde123456789/appointments");
        officerLinkTypes.setAppointments("/company/01777777/appointments/123456789abcde");
        linksItem.setOfficer(officerLinkTypes);
        return Collections.singletonList(linksItem);
    }

    private ItemLinkTypes buildLinksItem() {
        ItemLinkTypes linksItem = new ItemLinkTypes();
        linksItem.setSelf("/officers/abcde123456789/full_record");
        OfficerLinkTypes officerLinkTypes = new OfficerLinkTypes();
        officerLinkTypes.setAppointments("/company/01777777/appointments/123456789abcde");
        linksItem.setOfficer(officerLinkTypes);
        return linksItem;
    }

    private ContactDetails buildContactDetails() {
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setContactName("John Smith");
        return contactDetails;
    }
}
