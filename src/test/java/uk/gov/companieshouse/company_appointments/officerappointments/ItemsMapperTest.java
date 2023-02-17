package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.CorporateIdent.IdentificationTypeEnum;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;
import uk.gov.companieshouse.company_appointments.model.data.LinksData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerLinksData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

class ItemsMapperTest {

    private ItemsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ItemsMapper(new AddressMapper(),
                new ContactDetailsMapper(), new NameMapper(),
                new LocalDateMapper(), new FormerNamesMapper(),
                new IdentificationMapper(
                        new IdentificationTypeMapper()),
                new OfficerRoleMapper());
    }

    @Test
    void mapItems() {
        // given
        List<CompanyAppointmentData> appointmentList = getAppointmentList();

        List<OfficerAppointmentSummary> expected = getOfficerAppointments();
        // when
        List<OfficerAppointmentSummary> actual = mapper.map(appointmentList);

        // then
        assertEquals(expected, actual);
    }

    private List<CompanyAppointmentData> getAppointmentList() {
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

        return singletonList(data);
    }

    private List<OfficerAppointmentSummary> getOfficerAppointments() {
        return singletonList(new OfficerAppointmentSummary()
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
                        .identificationType(IdentificationTypeEnum.EEA)
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
                .officerRole(OfficerRoleEnum.DIRECTOR)
                .resignedOn(LocalDate.of(2020, 1, 1)));
    }
}