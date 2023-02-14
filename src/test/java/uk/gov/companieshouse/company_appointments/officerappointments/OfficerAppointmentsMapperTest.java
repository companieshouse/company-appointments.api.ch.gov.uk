package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerLinksData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

class OfficerAppointmentsMapperTest {

    private OfficerAppointmentsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OfficerAppointmentsMapper();
    }

    // TODO test corporate-managing-officer role for contact details, principal office address and responsibilities

    @Test
    @DisplayName("Should map officer appointments aggregate to an officer appointments api")
    void testMap() {
        // given
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate();
        AppointmentList expected = getExpectedOfficerAppointments();

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(officerAppointmentsAggregate);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregate() {
        TotalResults totalResults = new TotalResults();
        totalResults.setCount(1L);

        OfficerData officerData = OfficerData.builder()
                .withCompanyNumber("12345678")
                .withEtag("etag")
                .withOfficerRole("director")
                .withTitle("Mrs")
                .withFormerNames(singletonList(new FormerNamesData("former former", "names")))
                .withIdentification(new IdentificationData("eea", "legalAuth",
                        "legalForm", "placeReg", "regNumber"))
                .withDateOfBirth(LocalDateTime.of(2000, 1, 1, 0, 0))
                .withCountryOfResidence("UK")
                .withIsPre1992Appointment(false)
                .withLinks(new LinksData(
                        new OfficerLinksData("/officers/officerId", "/officers/officerId/id"),
                        "/company/12345678/appointments/id"))
                .withNationality("British")
                .withForename("forename")
                .withSurname("surname")
                .withHonours("FCA")
                .withResignedOn(LocalDateTime.of(2020, 1, 1, 0, 0))
                .withOtherForenames("secondForename")
                .withAppointedOn(LocalDateTime.of(2018, 1, 1, 0, 0))
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
        data.setCompanyName("company name");

        OfficerAppointmentsAggregate officerAppointmentsAggregate = new OfficerAppointmentsAggregate();
        officerAppointmentsAggregate.setTotalResults(totalResults);
        officerAppointmentsAggregate.setOfficerAppointments(singletonList(data));
        return officerAppointmentsAggregate;
    }

    private AppointmentList getExpectedOfficerAppointments() {
        return new AppointmentList()
                .dateOfBirth(new DateOfBirth()
                        .year(2000)
                        .month(1))
                .etag("etag")
                .isCorporateOfficer(false)
                .itemsPerPage(35)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self("/officers/officerId/appointments"))
                .items(singletonList(new OfficerAppointmentSummary()
                        .address(new Address()
                                .addressLine1("1 Crown Way")
                                .addressLine2("Pavement")
                                .careOf("careOf")
                                .country("UK")
                                .locality("Cardiff")
                                .poBox("poBox")
                                .postalCode("CF14 3UZ")
                                .premises("premises")
                                .region("Cardiff"))
                        .appointedBefore(LocalDate.of(1993, 3, 13))
                        .appointedOn(LocalDate.of(2018, 1, 1))
                        .appointedTo(new AppointedTo()
                                .companyName("company name")
                                .companyNumber("12345678")
                                .companyStatus("active"))
                        .name("forename secondForename surname")
                        .countryOfResidence("UK")
                        .formerNames(singletonList(new FormerNames()
                                .forenames("former former")
                                .surname("names")))
                        .identification(new CorporateIdent()
                                .identificationType(CorporateIdent.IdentificationTypeEnum.EEA)
                                .legalAuthority("legalAuth")
                                .legalForm("legalForm")
                                .placeRegistered("placeReg")
                                .registrationNumber("regNumber"))
                        .isPre1992Appointment(false)
                        .links(new AppointmentLinkTypes().company("/company/12345678"))
                        .nameElements(new NameElements()
                                .forename("forename")
                                .title("Mrs")
                                .otherForenames("secondForename")
                                .surname("surname")
                                .honours("FCA"))
                        .nationality("British")
                        .occupation("Company Director")
                        .officerRole(OfficerAppointmentSummary.OfficerRoleEnum.DIRECTOR)
                        .resignedOn(LocalDate.of(2020, 1, 1))))
                .name("forename secondForename surname")
                .startIndex(0)
                .totalResults(1);
    }
}