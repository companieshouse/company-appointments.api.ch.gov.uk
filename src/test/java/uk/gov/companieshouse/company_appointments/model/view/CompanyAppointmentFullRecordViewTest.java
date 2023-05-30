package uk.gov.companieshouse.company_appointments.model.view;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.companieshouse.company_appointments.model.data.DeltaUsualResidentialAddress;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordViewTest {

    private static final Instant INSTANT = Instant.from(LocalDate.of(1989, 1, 12).atStartOfDay(UTC));

    private final List<DeltaFormerNames> formerNames
            = buildFormerNamesList();
    private final DeltaIdentification identification = buildIdentification();
    private final DeltaItemLinkTypes links = buildLinksItem();
    private final DeltaContactDetails contactDetails = buildContactDetails();
    private final DateOfBirthView dob = new DateOfBirthView(12,1,1989);

    private CompanyAppointmentFullRecordView testView;

    @BeforeEach
    void setUp() {

        CompanyAppointmentDocument companyAppointmentDocument = new CompanyAppointmentDocument();
        companyAppointmentDocument.setData(new DeltaOfficerData());
        companyAppointmentDocument.setSensitiveData(new DeltaSensitiveData());

        companyAppointmentDocument.getData().setServiceAddress(createServiceAddress());
        companyAppointmentDocument.getSensitiveData().setUsualResidentialAddress(createUsualResidentialAddress());
        companyAppointmentDocument.getData().setAppointedOn(INSTANT);
        companyAppointmentDocument.getData().setAppointedBefore(INSTANT);
        companyAppointmentDocument.getData().setCountryOfResidence("countryOfResidence");
        companyAppointmentDocument.getSensitiveData().setDateOfBirth(INSTANT);
        companyAppointmentDocument.getData().setFormerNames(formerNames);
        companyAppointmentDocument.getData().setIdentification(identification);
        companyAppointmentDocument.getData().setLinks(buildLinksItem());
        companyAppointmentDocument.getData().setSurname("Davies");
        companyAppointmentDocument.getData().setForename("James");
        companyAppointmentDocument.getData().setTitle("Sir");
        companyAppointmentDocument.getData().setNationality("Welsh");
        companyAppointmentDocument.getData().setOccupation("occupation");
        companyAppointmentDocument.getData().setOfficerRole("director");
        companyAppointmentDocument.getData().setResignedOn(INSTANT);
        companyAppointmentDocument.getData().setEtag("etag");
        companyAppointmentDocument.getData().setPersonNumber("1234");
        companyAppointmentDocument.getData().setPre1992Appointment(Boolean.TRUE);
        companyAppointmentDocument.getData().setContactDetails(contactDetails);
        companyAppointmentDocument.getData().setResponsibilities("responsibilities");
        companyAppointmentDocument.getData().setPrincipalOfficeAddress(createPrincipalOfficeAddress());

        testView = CompanyAppointmentFullRecordView.Builder.view(companyAppointmentDocument).build();
    }

    @Test
    void serviceAddress() {

        checkServiceAddress(testView.getServiceAddress());
    }

    @Test
    void usualResidentialAddress() {

        checkUsualResidentialAddress(testView.getUsualResidentialAddress());
    }

    @Test
    void principleOfficeAddress() {

        checkPrincipleOfficeAddress(testView.getPrincipalOfficeAddress());
    }

    private void checkUsualResidentialAddress(DeltaUsualResidentialAddress address) {

        assertThat(address.getAddressLine1(), is(String.join(" ", "usualResidential", "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", "usualResidential", "address2")));
        assertThat(address.getCareOf(), is(String.join(" ", "usualResidential", "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", "usualResidential", "country")));
        assertThat(address.getLocality(), is(String.join(" ", "usualResidential", "locality")));
        assertThat(address.getPoBox(), is(String.join(" ", "usualResidential", "poBox")));
        assertThat(address.getPostalCode(), is(String.join(" ", "usualResidential", "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", "usualResidential", "premises")));
        assertThat(address.getRegion(), is(String.join(" ", "usualResidential", "region")));
    }

    private void checkServiceAddress(DeltaServiceAddress address) {

        assertThat(address.getAddressLine1(), is(String.join(" ", "service", "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", "service", "address2")));
        assertThat(address.getCountry(), is(String.join(" ", "service", "country")));
        assertThat(address.getLocality(), is(String.join(" ", "service", "locality")));
        assertThat(address.getPostalCode(), is(String.join(" ", "service", "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", "service", "premises")));
        assertThat(address.getRegion(), is(String.join(" ", "service", "region")));
    }

    private void checkPrincipleOfficeAddress(DeltaPrincipalOfficeAddress address) {

        assertThat(address.getAddressLine1(), is(String.join(" ", "principleOffice", "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", "principleOffice", "address2")));
        assertThat(address.getCareOf(), is(String.join(" ", "principleOffice", "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", "principleOffice", "country")));
        assertThat(address.getLocality(), is(String.join(" ", "principleOffice", "locality")));
        assertThat(address.getPoBox(), is(String.join(" ", "principleOffice", "poBox")));
        assertThat(address.getPostalCode(), is(String.join(" ", "principleOffice", "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", "principleOffice", "premises")));
        assertThat(address.getRegion(), is(String.join(" ", "principleOffice", "region")));
    }

    @Test
    void appointedOn() {

        assertThat(testView.getAppointedOn(), is(LocalDate.from(INSTANT.atZone(UTC))));
    }

    @Test
    void appointedBefore() {

        assertThat(testView.getAppointedBefore(), is(LocalDate.from(INSTANT.atZone(UTC))));
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

        assertThat(testView.getResignedOn(), is(LocalDate.from(INSTANT.atZone(UTC))));
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

    private DeltaServiceAddress createServiceAddress() {

        DeltaServiceAddress address = new DeltaServiceAddress();
        address.setAddressLine1(String.join(" ", "service", "address1"));
        address.setAddressLine2(String.join(" ", "service", "address2"));
        address.setCountry(String.join(" ", "service", "country"));
        address.setLocality(String.join(" ", "service", "locality"));
        address.setPostalCode(String.join(" ", "service", "postcode"));
        address.setPremises(String.join(" ", "service", "premises"));
        address.setRegion(String.join(" ", "service", "region"));

        return address;
    }

    private DeltaUsualResidentialAddress createUsualResidentialAddress() {

        DeltaUsualResidentialAddress address = new DeltaUsualResidentialAddress();
        address.setAddressLine1(String.join(" ", "usualResidential", "address1"));
        address.setAddressLine2(String.join(" ", "usualResidential", "address2"));
        address.setCareOf(String.join(" ", "usualResidential", "careOf"));
        address.setCountry(String.join(" ", "usualResidential", "country"));
        address.setLocality(String.join(" ", "usualResidential", "locality"));
        address.setPoBox(String.join(" ", "usualResidential", "poBox"));
        address.setPostalCode(String.join(" ", "usualResidential", "postcode"));
        address.setPremises(String.join(" ", "usualResidential", "premises"));
        address.setRegion(String.join(" ", "usualResidential", "region"));

        return address;
    }

    private DeltaPrincipalOfficeAddress createPrincipalOfficeAddress() {

        DeltaPrincipalOfficeAddress address = new DeltaPrincipalOfficeAddress();
        address.setAddressLine1(String.join(" ", "principleOffice", "address1"));
        address.setAddressLine2(String.join(" ", "principleOffice", "address2"));
        address.setCareOf(String.join(" ", "principleOffice", "careOf"));
        address.setCountry(String.join(" ", "principleOffice", "country"));
        address.setLocality(String.join(" ", "principleOffice", "locality"));
        address.setPoBox(String.join(" ", "principleOffice", "poBox"));
        address.setPostalCode(String.join(" ", "principleOffice", "postcode"));
        address.setPremises(String.join(" ", "principleOffice", "premises"));
        address.setRegion(String.join(" ", "principleOffice", "region"));

        return address;
    }

    private List<DeltaFormerNames> buildFormerNamesList() {
        DeltaFormerNames formerNamesItems = new DeltaFormerNames();
        formerNamesItems.setForenames("John");
        formerNamesItems.setSurname("Davies");
        return Collections.singletonList(formerNamesItems);
    }

    private DeltaIdentification buildIdentification() {
        DeltaIdentification identification = new DeltaIdentification();
        identification.setIdentificationType("eea");
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
