package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;

@ExtendWith(MockitoExtension.class)
class ItemsMapperTest {

    @InjectMocks
    private ItemsMapper mapper;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private Address address;
    @Mock
    private Address principalOfficeAddress;
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
    private DeltaServiceAddress serviceAddressData;
    @Mock
    private DeltaPrincipalOfficeAddress deltaPrincipalOfficeAddress;
    @Mock
    private DeltaContactDetails contactDetailsData;
    @Mock
    private DeltaFormerNames formerNamesData;
    @Mock
    private DeltaIdentification identificationData;
    @Mock
    private NameElements nameElements;

    private final Instant appointedBefore = Instant.from(
            LocalDate.of(1993, 3, 13).atStartOfDay(ZoneOffset.UTC));
    private final Instant appointedOn = Instant.from(
            LocalDate.of(2018, 1, 1).atStartOfDay(ZoneOffset.UTC));
    private final Instant resignedOn = Instant.from(
            LocalDate.of(2019, 1, 1).atStartOfDay(ZoneOffset.UTC));

    @Test
    @DisplayName("Should map a list of appointments to a list of officer appointments")
    void mapItems() {
        // given
        when(addressMapper.map(any(DeltaServiceAddress.class))).thenReturn(address);
        when(addressMapper.map(any(DeltaPrincipalOfficeAddress.class))).thenReturn(
                principalOfficeAddress);
        when(contactDetailsMapper.map(any())).thenReturn(contactDetails);
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(localDateMapper.map(appointedBefore)).thenReturn(
                LocalDate.of(1993, 3, 13)); // was a string
        when(localDateMapper.map(appointedOn)).thenReturn(LocalDate.of(2018, 1, 1));
        when(localDateMapper.map(resignedOn)).thenReturn(LocalDate.of(2019, 1, 1));
        when(formerNamesMapper.map(any())).thenReturn(singletonList(formerNames));
        when(identificationMapper.map(any())).thenReturn(corporateIdent);
        when(roleMapper.mapOfficerRole(anyString())).thenReturn(OfficerRoleEnum.DIRECTOR);
        when(nameMapper.mapNameElements(any())).thenReturn(nameElements);

        List<CompanyAppointmentDocument> appointmentList = getAppointmentList();

        List<OfficerAppointmentSummary> expected = getOfficerAppointments();
        // when
        List<OfficerAppointmentSummary> actual = mapper.map(appointmentList);

        // then
        assertEquals(expected, actual);
        verify(addressMapper).map(serviceAddressData);
        verify(addressMapper).map(deltaPrincipalOfficeAddress);
        verify(localDateMapper).map(
                LocalDate.of(1993, 3, 13).atStartOfDay().toInstant(ZoneOffset.UTC));
        verify(localDateMapper).map(LocalDateTime.of(2018, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
        verify(localDateMapper).map(LocalDateTime.of(2019, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
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
        List<CompanyAppointmentDocument> appointmentList = emptyList();

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
        List<CompanyAppointmentDocument> appointmentList = singletonList(
                new CompanyAppointmentDocument());

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

    private List<CompanyAppointmentDocument> getAppointmentList() {
        DeltaOfficerData officerData = buildOfficerData();

        CompanyAppointmentDocument data = new CompanyAppointmentDocument()
                .companyStatus("active")
                .data(officerData)
                .companyName("company name");

        return singletonList(data);
    }

    private DeltaOfficerData buildOfficerData() {
        return new DeltaOfficerData()
                .setCompanyNumber("12345678")
                .setOfficerRole("director")
                .setTitle("Mrs")
                .setFormerNames(singletonList(formerNamesData))
                .setIdentification(identificationData)
                .setCountryOfResidence("UK")
                .setContactDetails(contactDetailsData)
                .setPre1992Appointment(false)
                .setNationality("British")
                .setForename("forename")
                .setSurname("surname")
                .setHonours("FCA")
                .setResignedOn(resignedOn)
                .setOtherForenames("secondForename")
                .setAppointedOn(appointedOn)
                .setAppointedBefore(appointedBefore)
                .setOccupation("Company Director")
                .setServiceAddress(serviceAddressData)
                .setPrincipalOfficeAddress(deltaPrincipalOfficeAddress);
    }

    private List<OfficerAppointmentSummary> getOfficerAppointments() {
        return singletonList(new OfficerAppointmentSummary()
                .address(address)
                .appointedBefore(LocalDate.from(appointedBefore.atZone(ZoneOffset.UTC)))
                .appointedOn(LocalDate.from(appointedOn.atZone(ZoneOffset.UTC)))
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
                .principalOfficeAddress(principalOfficeAddress)
                .resignedOn(LocalDate.from(resignedOn.atZone(ZoneOffset.UTC))));
    }
}
