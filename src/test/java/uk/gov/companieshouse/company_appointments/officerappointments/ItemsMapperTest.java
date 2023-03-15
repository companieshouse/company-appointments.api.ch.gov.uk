package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;
import uk.gov.companieshouse.company_appointments.model.data.IdentificationData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

@ExtendWith(MockitoExtension.class)
class ItemsMapperTest {

    @InjectMocks
    private ItemsMapper mapper;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private Address address;
    @Mock
    private ContactDetailsMapper contactDetailsMapper;
    @Mock
    private ContactDetails contactDetails;
    @Mock
    private NameMapper nameMapper;
    @Mock
    private LocalDateMapper localDateMapper;
    @Mock
    private FormerNamesMapper formerNamesMapper;
    @Mock
    private FormerNames formerNames;
    @Mock
    private IdentificationMapper identificationMapper;
    @Mock
    private CorporateIdent corporateIdent;
    @Mock
    private OfficerRoleMapper roleMapper;
    @Mock
    private ServiceAddressData serviceAddressData;
    @Mock
    private ContactDetailsData contactDetailsData;
    @Mock
    private FormerNamesData formerNamesData;
    @Mock
    private IdentificationData identificationData;
    @Mock
    private NameElements nameElements;

    @Test
    @DisplayName("Should map a list of appointments to a list of officer appointments")
    void mapItems() {
        // given
        when(addressMapper.map(any())).thenReturn(address);
        when(contactDetailsMapper.map(any())).thenReturn(contactDetails);
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(localDateMapper.map(anyString())).thenReturn(LocalDate.of(1993, 3, 13));
        when(localDateMapper.map((LocalDateTime) any())).thenReturn(LocalDate.of(2018, 1, 1));
        when(formerNamesMapper.map(any())).thenReturn(singletonList(formerNames));
        when(identificationMapper.map(any())).thenReturn(corporateIdent);
        when(roleMapper.mapOfficerRole(anyString())).thenReturn(OfficerRoleEnum.DIRECTOR);
        when(nameMapper.mapNameElements(any())).thenReturn(nameElements);

        List<CompanyAppointmentData> appointmentList = getAppointmentList();

        List<OfficerAppointmentSummary> expected = getOfficerAppointments();
        // when
        List<OfficerAppointmentSummary> actual = mapper.map(appointmentList);

        // then
        assertEquals(expected, actual);
        verify(addressMapper, times(2)).map(serviceAddressData);
        verify(localDateMapper).map("1993-03-13");
        verify(localDateMapper, times(2)).map(LocalDateTime.of(2020, 1, 1, 0, 0));
        verify(contactDetailsMapper).map(contactDetailsData);
        verify(nameMapper).map(buildOfficerData());
        verify(formerNamesMapper).map(singletonList(formerNamesData));
        verify(identificationMapper).map(identificationData);
        verify(roleMapper).mapOfficerRole("director");
    }

    @Test
    @DisplayName("Should return an empty list if the list of appointments is empty")
    void mapEmptyAppointmentsList() {
        // given
        List<CompanyAppointmentData> appointmentList = emptyList();

        // when
        List<OfficerAppointmentSummary> actual = mapper.map(appointmentList);

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(addressMapper);
        verifyNoInteractions(localDateMapper);
        verifyNoInteractions(localDateMapper);
        verifyNoInteractions(contactDetailsMapper);
        verifyNoInteractions(nameMapper);
        verifyNoInteractions(formerNamesMapper);
        verifyNoInteractions(identificationMapper);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("Should return an empty optional if the list of appointments within the aggregate has null officer data")
    void mapNullOfficerData() {
        // given
        List<CompanyAppointmentData> appointmentList = singletonList(new CompanyAppointmentData());

        // when
        List<OfficerAppointmentSummary> actual = mapper.map(appointmentList);

        // then
        assertEquals(singletonList(new OfficerAppointmentSummary()), actual);
        verifyNoInteractions(addressMapper);
        verifyNoInteractions(localDateMapper);
        verifyNoInteractions(localDateMapper);
        verifyNoInteractions(contactDetailsMapper);
        verifyNoInteractions(nameMapper);
        verifyNoInteractions(formerNamesMapper);
        verifyNoInteractions(identificationMapper);
        verifyNoInteractions(roleMapper);
    }

    private List<CompanyAppointmentData> getAppointmentList() {
        OfficerData officerData = buildOfficerData();

        CompanyAppointmentData data = new CompanyAppointmentData();
        data.setCompanyStatus("active");
        data.setData(officerData);
        data.setCompanyName("company name");

        return singletonList(data);
    }

    private OfficerData buildOfficerData() {
        return OfficerData.builder()
                .withCompanyNumber("12345678")
                .withOfficerRole("director")
                .withTitle("Mrs")
                .withFormerNames(singletonList(formerNamesData))
                .withIdentification(identificationData)
                .withCountryOfResidence("UK")
                .withContactDetails(contactDetailsData)
                .withIsPre1992Appointment(false)
                .withNationality("British")
                .withForename("forename")
                .withSurname("surname")
                .withHonours("FCA")
                .withResignedOn(LocalDateTime.of(2020, 1, 1, 0, 0))
                .withOtherForenames("secondForename")
                .withAppointedOn(LocalDateTime.of(2020, 1, 1, 0, 0))
                .withAppointedBefore("1993-03-13")
                .withOccupation("Company Director")
                .withServiceAddress(serviceAddressData)
                .withPrincipalOfficeAddress(serviceAddressData)
                .build();
    }

    private List<OfficerAppointmentSummary> getOfficerAppointments() {
        return singletonList(new OfficerAppointmentSummary()
                .address(address)
                .appointedBefore(LocalDate.of(1993, 3, 13))
                .appointedOn(LocalDate.of(2018, 1, 1))
                .appointedTo(new AppointedTo()
                        .companyName("company name")
                        .companyNumber("12345678")
                        .companyStatus("active"))
                .name("forename secondForename surname")
                .contactDetails(contactDetails)
                .countryOfResidence("UK")
                .formerNames(singletonList(formerNames))
                .identification(corporateIdent)
                .isPre1992Appointment(false)
                .links(new AppointmentLinkTypes().company("/company/12345678"))
                .nameElements(nameElements)
                .nationality("British")
                .occupation("Company Director")
                .officerRole(OfficerRoleEnum.DIRECTOR)
                .principalOfficeAddress(address)
                .resignedOn(LocalDate.of(2018, 1, 1)));
    }
}