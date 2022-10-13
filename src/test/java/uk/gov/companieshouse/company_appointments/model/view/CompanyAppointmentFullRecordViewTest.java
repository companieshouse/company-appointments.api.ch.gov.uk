package uk.gov.companieshouse.company_appointments.model.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;

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

        OfficerAPI officerData = new OfficerAPI();
        SensitiveOfficerAPI sensitiveOfficer = new SensitiveOfficerAPI();
        officerData.setServiceAddress(createAddress("service"));
        sensitiveOfficer.setUsualResidentialAddress(createAddress("usualResidential"));
        officerData.setAppointedOn(INSTANT_ONE);
        officerData.setAppointedBefore(INSTANT_THREE);
        officerData.setCountryOfResidence("countryOfResidence");
        sensitiveOfficer.setDateOfBirth(INSTANT_DOB);
        officerData.setFormerNameData(formerNames);
        officerData.setIdentificationData(identification);
        officerData.setLinksData(links);
        officerData.setSurname("Davies");
        officerData.setForename("James");
        officerData.setTitle("Sir");
        officerData.setNationality("Welsh");
        officerData.setOccupation("occupation");
        officerData.setOfficerRole("director");
        officerData.setResignedOn(INSTANT_TWO);

        testView = CompanyAppointmentFullRecordView.Builder.view(officerData, sensitiveOfficer).build();
    }

    @Test
    void serviceAddress() {

        checkAddress(testView.getServiceAddress(), "service");
    }

    @Test
    void usualResidentialAddress() {

        checkAddress(testView.getUsualResidentialAddress(), "usualResidential");
    }

    private void checkAddress(AddressAPI address, String prefix) {

        assertThat(address.getAddressLine1(), is(String.join(" ", "prefix", "address1")));
        assertThat(address.getAddressLine2(), is(String.join(" ", "prefix", "address2")));
        assertThat(address.getCareOf(), is(String.join(" ", "prefix", "careOf")));
        assertThat(address.getCountry(), is(String.join(" ", "prefix", "country")));
        assertThat(address.getLocality(), is(String.join(" ", "prefix", "locality")));
        assertThat(address.getPoBox(), is(String.join(" ", "prefix", "poBox")));
        assertThat(address.getPostcode(), is(String.join(" ", "prefix", "postcode")));
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

    private AddressAPI createAddress(String prefix) {

        AddressAPI address = new AddressAPI();
        address.setAddressLine1(String.join(" ", "prefix", "address1"));
        address.setAddressLine2(String.join(" ", "prefix", "address2"));
        address.setCareOf(String.join(" ", "prefix", "careOf"));
        address.setCountry(String.join(" ", "prefix", "country"));
        address.setLocality(String.join(" ", "prefix", "locality"));
        address.setPoBox(String.join(" ", "prefix", "poBox"));
        address.setPostcode(String.join(" ", "prefix", "postcode"));
        address.setPremises(String.join(" ", "prefix", "premises"));
        address.setRegion(String.join(" ", "prefix", "region"));

        return address;
    }
}
