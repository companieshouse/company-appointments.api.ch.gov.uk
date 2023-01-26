package uk.gov.companieshouse.company_appointments.model.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordViewTest {

    private static final Instant INSTANT_DOB = Instant.parse("1989-01-12T00:00:00.000Z");
    private static final Instant INSTANT_ONE = Instant.parse("2009-01-12T00:00:00.000Z");
    private static final Instant INSTANT_TWO = Instant.parse("2019-01-12T00:00:00.000Z");
    private static final Instant INSTANT_THREE = Instant.parse("1991-01-12T00:00:00.000Z");


    private List<FormerNamesAPI> formerNames
        = Collections.singletonList(new FormerNamesAPI("John", "Davies"));
    private IdentificationAPI identification
        = new IdentificationAPI("eea","Chapter 32","Hong Kong","UK","32982");
    private LinksAPI links =
        new LinksAPI("/officers/abcde123456789","/officers/abcde123456789/appointments","/company/01777777/appointments/123456789abcde");
    private DateOfBirth dob = new DateOfBirth(12,1,1989);

    private CompanyAppointmentFullRecordView testView;

    @BeforeEach
    void setUp() {

        DeltaAppointmentApi deltaAppointmentApi = new DeltaAppointmentApi();

//        OfficerAPI officerData = new OfficerAPI();
//        SensitiveOfficerAPI sensitiveOfficer = new SensitiveOfficerAPI();
        deltaAppointmentApi.getData().setServiceAddress(createServiceAddress("service"));
        deltaAppointmentApi.getSensitiveData().setUsualResidentialAddress(createUsualResidentialAddress("usualResidential"));
        deltaAppointmentApi.getData().setAppointedOn(INSTANT_ONE);
        deltaAppointmentApi.getData().setAppointedBefore(INSTANT_THREE);
        deltaAppointmentApi.getData().setCountryOfResidence("countryOfResidence");
        deltaAppointmentApi.getSensitiveData().setDateOfBirth(INSTANT_DOB);
        deltaAppointmentApi.getData().setFormerNames(formerNames);;
        deltaAppointmentApi.getData().setIdentification(identification);
        deltaAppointmentApi.getData().setLinks(links);
        deltaAppointmentApi.getData().setSurname("Davies");
        deltaAppointmentApi.getData().setForename("James");
        deltaAppointmentApi.getData().setTitle("Sir");
        deltaAppointmentApi.getData().setNationality("Welsh");
        deltaAppointmentApi.getData().setOccupation("occupation");
        deltaAppointmentApi.getData().setOfficerRole("director");
        deltaAppointmentApi.getData().setResignedOn(INSTANT_TWO);
        deltaAppointmentApi.setEtag("etag");

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

    private void checkUsualResidentialAddress(UsualResidentialAddress address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", "prefix", "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", "prefix", "address2")));
        assertThat(address.getCareOf(), is(String.join(" ", "prefix", "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", "prefix", "country")));
        assertThat(address.getLocality(), is(String.join(" ", "prefix", "locality")));
        assertThat(address.getPoBox(), is(String.join(" ", "prefix", "poBox")));
        assertThat(address.getPostalCode(), is(String.join(" ", "prefix", "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", "prefix", "premises")));
        assertThat(address.getRegion(), is(String.join(" ", "prefix", "region")));
    }

    private void checkServiceAddress(ServiceAddress address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", "prefix", "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", "prefix", "address2")));
        //assertThat(address.getCareOf(), is(String.join(" ", "prefix", "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", "prefix", "country")));
        assertThat(address.getLocality(), is(String.join(" ", "prefix", "locality")));
        //assertThat(address.getPoBox(), is(String.join(" ", "prefix", "poBox")));
        assertThat(address.getPostalCode(), is(String.join(" ", "prefix", "postcode")));
        assertThat(address.getPremises(), is(String.join(" ", "prefix", "premises")));
        assertThat(address.getRegion(), is(String.join(" ", "prefix", "region")));
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

    private ServiceAddress createServiceAddress(String prefix) {

        ServiceAddress address = new ServiceAddress();
        address.setAddressLine1(String.join(" ", "prefix", "address1"));
        address.setAddressLine2(String.join(" ", "prefix", "address2"));
        //address.setCareOf(String.join(" ", "prefix", "careOf"));
        address.setCountry(String.join(" ", "prefix", "country"));
        address.setLocality(String.join(" ", "prefix", "locality"));
        //address.setPoBox(String.join(" ", "prefix", "poBox"));
        //address.setPostcode(String.join(" ", "prefix", "postcode"));
        address.setPremises(String.join(" ", "prefix", "premises"));
        address.setRegion(String.join(" ", "prefix", "region"));

        return address;
    }

    private UsualResidentialAddress createUsualResidentialAddress(String prefix) {

        UsualResidentialAddress address = new UsualResidentialAddress();
        address.setAddressLine1(String.join(" ", "prefix", "address1"));
        address.setAddressLine2(String.join(" ", "prefix", "address2"));
        address.setCareOf(String.join(" ", "prefix", "careOf"));
        address.setCountry(String.join(" ", "prefix", "country"));
        address.setLocality(String.join(" ", "prefix", "locality"));
        address.setPoBox(String.join(" ", "prefix", "poBox"));
        address.setPostalCode(String.join(" ", "prefix", "postcode"));
        address.setPremises(String.join(" ", "prefix", "premises"));
        address.setRegion(String.join(" ", "prefix", "region"));

        return address;
    }
}
