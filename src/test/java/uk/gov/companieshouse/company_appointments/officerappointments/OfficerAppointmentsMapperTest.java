package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.common.Address;
import uk.gov.companieshouse.api.model.common.DateOfBirth;
import uk.gov.companieshouse.api.model.common.Links;
import uk.gov.companieshouse.api.model.officerappointments.AppointedToApi;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentLinks;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentsNameElements;
import uk.gov.companieshouse.api.model.officerappointments.OfficerAppointmentsApi;
import uk.gov.companieshouse.api.model.officers.FormerNamesApi;
import uk.gov.companieshouse.api.model.officers.IdentificationApi;
import uk.gov.companieshouse.api.model.officers.OfficerRoleApi;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerLinksData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

class OfficerAppointmentsMapperTest {

    private OfficerAppointmentsMapper mapper;

    @Test
    @DisplayName("Should map officer appointments aggregate to an officer appointments api")
    void testMap() {
        // given
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate();

        // when
        OfficerAppointmentsApi actual = mapper.map(officerAppointmentsAggregate);

        // then
        assertEquals(getExpectedOfficerAppointmentsApi(), actual);
    }

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregate() {
        TotalResults totalResults = new TotalResults();
        totalResults.setCount(1L);

        OfficerData officerData = OfficerData.builder()
                .withCompanyNumber("12345678")
                .withCompanyName("company name")
                .withOfficerRole("director")
                .withTitle("Mrs")
                .withFormerNames(singletonList(new FormerNamesData("former former", "names")))
                .withIdentification(new IdentificationData("idType", "legalAuth",
                        "legalForm", "placeReg", "regNumber"))
                .withDateOfBirth(LocalDateTime.of(2000, 1, 1,0,0))
                .withCountryOfResidence("UK")
                .withIsPre1992Appointment(false)
                .withLinks(new LinksData(
                        new OfficerLinksData("/officers/officerId","/officers/officerId/id"),
                        "/company/12345678/appointments/id"))
                .withNationality("British")
                .withForename("forename")
                .withSurname("surname")
                .withHonours("FCA")
                .withResignedOn(LocalDateTime.of(2020, 1, 1,0,0))
                .withOtherForenames("secondForename")
                .withAppointedOn(LocalDateTime.of(2018,1,1,0,0))
                .withAppointedBefore("1993-03-13")
                .withOccupation("Company Director")
                .withServiceAddress(new ServiceAddressData("1 Crown Way", "Pavement",
                        "careOf", "UK", "Cardiff", "poBox",
                        "CF14 3UZ", "premises", "Cardiff"))
                .build();

        CompanyAppointmentData data = new CompanyAppointmentData();
        data.setOfficerId("officerId");
        data.setId("id");
        data.setCompanyStatus("active");
        data.setData(officerData);

        OfficerAppointmentsAggregate officerAppointmentsAggregate = new OfficerAppointmentsAggregate();
        officerAppointmentsAggregate.setTotalResults(totalResults);
        officerAppointmentsAggregate.setOfficerAppointments(singletonList(data));
        return officerAppointmentsAggregate;
    }

    private OfficerAppointmentsApi getExpectedOfficerAppointmentsApi() {
        AppointedToApi appointedToApi = new AppointedToApi();
        appointedToApi.setCompanyStatus("active");
        appointedToApi.setCompanyNumber("12345678");
        appointedToApi.setCompanyName("company name");

        Address address = new Address();
        address.setAddressLine1("1 Crown Way");
        address.setAddressLine2("Pavement");
        address.setCareOf("careOf");
        address.setCountry("UK");
        address.setLocality("Cardiff");
        address.setPoBox("poBox");
        address.setPostalCode("CF14 3UZ");
        address.setPremises("premises");
        address.setRegion("Cardiff");

        AppointmentLinks appointmentLinks = new AppointmentLinks();
        appointmentLinks.setCompany("/company/12345678/appointments/id");

        FormerNamesApi formerNamesApi = new FormerNamesApi();
        formerNamesApi.setForenames("former former");
        formerNamesApi.setSurname("names");

        IdentificationApi identificationApi = new IdentificationApi();
        identificationApi.setIdentificationType("idType");
        identificationApi.setLegalAuthority("legalAuth");
        identificationApi.setLegalForm("legalForm");
        identificationApi.setPlaceRegistered("placeReg");
        identificationApi.setRegistrationNumber("regNumber");

        AppointmentsNameElements nameElements = new AppointmentsNameElements();
        nameElements.setTitle("Mrs");
        nameElements.setForename("forename");
        nameElements.setOtherForenames("secondForename");
        nameElements.setSurname("surname");
        nameElements.setHonours("FCA");

        AppointmentApi appointmentApi = new AppointmentApi();
        appointmentApi.setAppointedOn(LocalDate.of(2018,1,1));
        appointmentApi.setAppointedBefore(LocalDate.of(1993,3,13));
        appointmentApi.setAppointedTo(appointedToApi);
        appointmentApi.setAddress(address);
        appointmentApi.setLinks(appointmentLinks);
        appointmentApi.setCountryOfResidence("UK");
        appointmentApi.setName("forename secondForename surname");
        appointmentApi.setNationality("British");
        appointmentApi.setOccupation("Company Director");
        appointmentApi.setOfficerRole(OfficerRoleApi.DIRECTOR);
        appointmentApi.setResignedOn(LocalDate.of(2020,1,1));
        appointmentApi.setPre1992Appointment(false);
        appointmentApi.setFormerNames(singletonList(formerNamesApi));
        appointmentApi.setIdentification(identificationApi);
        appointmentApi.setNameElements(nameElements);

        DateOfBirth dateOfBirth = new DateOfBirth();
        dateOfBirth.setYear(2000L);
        dateOfBirth.setMonth(1L);

        Links links = new Links();
        links.setSelf("/officers/officerId/appointments");

        OfficerAppointmentsApi expected = new OfficerAppointmentsApi();
        expected.setTotalResults(2L);
        expected.setCorporateOfficer(false);
        expected.setDateOfBirth(dateOfBirth);
        expected.setItemsPerPage(35L);
        expected.setKind("kind");
        expected.setLinks(links);
        expected.setName("forename secondForename surname");
        expected.setStartIndex(0L);
        expected.setItems(singletonList(appointmentApi));
        return expected;
    }
}