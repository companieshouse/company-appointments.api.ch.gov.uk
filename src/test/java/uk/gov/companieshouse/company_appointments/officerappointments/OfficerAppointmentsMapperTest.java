package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaUsualResidentialAddress;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsMapperTest {

    private static final int START_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 35;
    public static final String DIRECTOR = "director";
    private static final String NAME = "forename secondForename surname";

    @InjectMocks
    private OfficerAppointmentsMapper mapper;
    @Mock
    private OfficerRoleMapper roleMapper;
    @Mock
    private ItemsMapper itemsMapper;
    @Mock
    private NameMapper nameMapper;
    @Mock
    private DateOfBirthMapper dobMapper;
    @Mock
    private DateOfBirth dateOfBirth;
    @Mock
    private OfficerAppointmentSummary officerAppointmentSummary;

    @Test
    @DisplayName("Should map first appointment and list to an API response with appointment counts")
    void mapOfficerAppointments() {
        // given
        DeltaOfficerData officerData = getOfficerData();
        CompanyAppointmentDocument firstAppointment = getAppointmentDocument(officerData, getSensitiveOfficerData());
        List<CompanyAppointmentDocument> documents = List.of(firstAppointment);

        List<OfficerAppointmentSummary> expectedItems = List.of(officerAppointmentSummary);

        AppointmentList expected = getExpectedAppointmentList(expectedItems);

        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);
        when(itemsMapper.map(any())).thenReturn(expectedItems);
        when(nameMapper.map(any())).thenReturn(NAME);
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(MapperRequest.builder()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(firstAppointment)
                .officerAppointments(documents)
                .totalResults(10)
                .inactiveCount(1)
                .resignedCount(2)
                .build());

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());

        verify(roleMapper).mapIsCorporateOfficer(DIRECTOR);
        verify(itemsMapper).map(documents);
        verify(nameMapper).map(officerData);
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), DIRECTOR);
    }

    @Test
    @DisplayName("Should return empty optional when first appointment is null")
    void mapOfficerAppointmentsNullFirstAppointment() {
        // given

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(MapperRequest.builder().build());

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(roleMapper);
        verifyNoInteractions(itemsMapper);
        verifyNoInteractions(nameMapper);
        verifyNoInteractions(dobMapper);
    }

    @Test
    @DisplayName("Should return empty optional when first appointment data is null")
    void mapOfficerAppointmentsNullFirstAppointmentData() {
        // given

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(MapperRequest.builder()
                .firstAppointment(getAppointmentDocument(null, getSensitiveOfficerData()))
                .build());

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(roleMapper);
        verifyNoInteractions(itemsMapper);
        verifyNoInteractions(nameMapper);
        verifyNoInteractions(dobMapper);
    }

    @Test
    @DisplayName("Should return empty optional when first appointment sensitive data is null")
    void mapOfficerAppointmentsNullFirstAppointmentSensitiveData() {
        // given
        DeltaOfficerData officerData = getOfficerData();
        CompanyAppointmentDocument firstAppointment = getAppointmentDocument(officerData, null);
        List<CompanyAppointmentDocument> documents = List.of(firstAppointment);

        List<OfficerAppointmentSummary> expectedItems = List.of(officerAppointmentSummary);
        AppointmentList expected = getExpectedAppointmentList(expectedItems)
                .dateOfBirth(null);

        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);
        when(itemsMapper.map(any())).thenReturn(expectedItems);
        when(nameMapper.map(any())).thenReturn(NAME);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(MapperRequest.builder()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(firstAppointment)
                .officerAppointments(documents)
                .totalResults(10)
                .inactiveCount(1)
                .resignedCount(2)
                .build());

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(roleMapper).mapIsCorporateOfficer(DIRECTOR);
        verify(itemsMapper).map(documents);
        verify(nameMapper).map(officerData);
        verifyNoInteractions(dobMapper);
    }

    private CompanyAppointmentDocument getAppointmentDocument(DeltaOfficerData officerData,
            DeltaSensitiveData sensitiveData) {
        return new CompanyAppointmentDocument()
                .officerId("officerId")
                .id("id")
                .companyStatus("active")
                .data(officerData)
                .sensitiveData(sensitiveData)
                .companyName("company name");
    }

    private DeltaSensitiveData getSensitiveOfficerData() {
        return new DeltaSensitiveData()
                .setUsualResidentialAddress(new DeltaUsualResidentialAddress()
                        .setAddressLine1("address-line-1")
                        .setAddressLine2("address-line-2")
                        .setCareOf("care-of")
                        .setCountry("United Kingdom")
                        .setLocality("Cardiff")
                        .setPoBox("po-box")
                        .setPostalCode("CF2 1B6")
                        .setPremises("URA")
                        .setRegion("ura-region"))
                .setResidentialAddressIsSameAsServiceAddress(false)
                .setDateOfBirth(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
    }

    private DeltaOfficerData getOfficerData() {
        return new DeltaOfficerData()
                .setEtag("etag")
                .setOfficerRole(OfficerAppointmentsMapperTest.DIRECTOR)
                .setTitle("Mrs");
    }

    private AppointmentList getExpectedAppointmentList(List<OfficerAppointmentSummary> items) {
        return new AppointmentList()
                .dateOfBirth(dateOfBirth)
                .etag("etag")
                .isCorporateOfficer(false)
                .itemsPerPage(ITEMS_PER_PAGE)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self("/officers/officerId/appointments"))
                .items(items)
                .name(NAME)
                .startIndex(START_INDEX)
                .activeCount(7)
                .inactiveCount(1)
                .resignedCount(2)
                .totalResults(10);
    }
}