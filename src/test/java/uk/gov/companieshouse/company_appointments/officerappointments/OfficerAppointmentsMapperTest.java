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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
    public static final String CORPORATE_MANAGING_OFFICER = "corporate-managing-officer";

    @InjectMocks
    private OfficerAppointmentsMapper mapper;
    @Mock
    private ItemsMapper itemsMapper;
    @Mock
    private NameMapper nameMapper;
    @Mock
    private DateOfBirthMapper dobMapper;
    @Mock
    private DateOfBirth dateOfBirth;
    @Mock
    private OfficerRoleMapper roleMapper;
    @Mock
    private OfficerAppointmentSummary officerAppointmentSummary;

    @Test
    @DisplayName("Should map officer appointments aggregate to an officer appointments api with appointment counts")
    void mapWithAppointmentCounts() {
        // given
        when(itemsMapper.map(any())).thenReturn(getMultipleAppointments());
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);

        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregateWithMultipleResults();
        CompanyAppointmentDocument CompanyAppointmentDocument = getCompanyAppointmentDocument(getOfficerData(DIRECTOR), getSensitiveOfficerData());
        List<CompanyAppointmentDocument> CompanyAppointmentDocumentList = getListOfCompanyAppointmentDocument();
        AppointmentList expected = getExpectedOfficerAppointmentsWithMultipleAppointments();

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(CompanyAppointmentDocument)
                .aggregate(officerAppointmentsAggregate));

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());

        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), DIRECTOR);
        verify(roleMapper).mapIsCorporateOfficer(DIRECTOR);
        verify(itemsMapper).map(CompanyAppointmentDocumentList);
        verify(nameMapper).map(getOfficerData(DIRECTOR));
    }

    @Test
    @DisplayName("Should map corporate managing officer appointments aggregate to an officer appointments api")
    void mapCorporateManagingOfficer() {
        // given
        when(itemsMapper.map(any())).thenReturn(singletonList(officerAppointmentSummary));
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(true);
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate();
        CompanyAppointmentDocument CompanyAppointmentDocument = getCompanyAppointmentDocument(
                getOfficerData(CORPORATE_MANAGING_OFFICER), getSensitiveOfficerData());

        AppointmentList expected = getExpectedOfficerAppointments(true);
        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(CompanyAppointmentDocument)
                .aggregate(officerAppointmentsAggregate));

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), CORPORATE_MANAGING_OFFICER);
        verify(roleMapper).mapIsCorporateOfficer(CORPORATE_MANAGING_OFFICER);
        verify(itemsMapper).map(singletonList(CompanyAppointmentDocument));
        verify(nameMapper).map(getOfficerData(CORPORATE_MANAGING_OFFICER));
    }

    @Test
    @DisplayName("Should return an appointment list with empty items and 0 total results if the list of appointments within the aggregate is empty")
    void mapEmptyAppointmentsList() {
        // given
        when(itemsMapper.map(any())).thenReturn(emptyList());
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);

        OfficerAppointmentsAggregate aggregate = new OfficerAppointmentsAggregate()
                .totalResults(0)
                .inactiveCount(0)
                .resignedCount(0);
        CompanyAppointmentDocument CompanyAppointmentDocument = getCompanyAppointmentDocument(getOfficerData(DIRECTOR), getSensitiveOfficerData());
        AppointmentList expected = getExpectedOfficerAppointments(false);
        expected.setItems(emptyList());
        expected.setTotalResults(0);
        expected.setActiveCount(0);
        expected.setInactiveCount(0);
        expected.setResignedCount(0);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(CompanyAppointmentDocument)
                .aggregate(aggregate));

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), DIRECTOR);
        verify(roleMapper).mapIsCorporateOfficer(DIRECTOR);
        verify(itemsMapper).map(emptyList());
        verify(nameMapper).map(getOfficerData(DIRECTOR));
    }

    @Test
    @DisplayName("Should return an empty optional if the list of appointments within the aggregate has null officer data")
    void mapNullOfficerData() {
        // given

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(getCompanyAppointmentDocument(null, null))
                .aggregate(new OfficerAppointmentsAggregate()));

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(dobMapper);
        verifyNoInteractions(roleMapper);
        verifyNoInteractions(itemsMapper);
        verifyNoInteractions(nameMapper);
    }

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregate() {
        CompanyAppointmentDocument data = getCompanyAppointmentDocument(getOfficerData(
                OfficerAppointmentsMapperTest.CORPORATE_MANAGING_OFFICER), getSensitiveOfficerData());
        return new OfficerAppointmentsAggregate()
                .totalResults(1)
                .officerAppointments(singletonList(data))
                .inactiveCount(0)
                .resignedCount(0);
    }

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregateWithMultipleResults() {
        List<CompanyAppointmentDocument> data = getListOfCompanyAppointmentDocument();

        return new OfficerAppointmentsAggregate()
                .totalResults(10)
                .officerAppointments(data)
                .inactiveCount(1)
                .resignedCount(2);
    }

    private List<CompanyAppointmentDocument> getListOfCompanyAppointmentDocument() {
        List<CompanyAppointmentDocument> CompanyAppointmentDocumentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompanyAppointmentDocumentList.add(getCompanyAppointmentDocument(getOfficerData(
                    OfficerAppointmentsMapperTest.DIRECTOR), getSensitiveOfficerData()));
        }
        return CompanyAppointmentDocumentList;
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
                .setResidentialAddressSameAsServiceAddress(false)
                .setDateOfBirth(LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
    }

    private CompanyAppointmentDocument getCompanyAppointmentDocument(DeltaOfficerData officerData,
            DeltaSensitiveData sensitiveData) {
        return new CompanyAppointmentDocument()
            .setOfficerId("officerId")
            .setId("id")
            .setCompanyStatus("active")
            .setData(officerData)
            .setSensitiveData(sensitiveData)
            .setCompanyName("company name");
    }

    private DeltaOfficerData getOfficerData(String role) {
        return new DeltaOfficerData()
                .setEtag("etag")
                .setOfficerRole(role)
                .setTitle("Mrs");
    }

    private AppointmentList getExpectedOfficerAppointments(boolean isCorporateOfficer) {
        return new AppointmentList()
                .dateOfBirth(dateOfBirth)
                .etag("etag")
                .isCorporateOfficer(isCorporateOfficer)
                .itemsPerPage(ITEMS_PER_PAGE)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self("/officers/officerId/appointments"))
                .items(singletonList(officerAppointmentSummary))
                .name("forename secondForename surname")
                .startIndex(START_INDEX)
                .totalResults(1)
                .activeCount(1)
                .inactiveCount(0)
                .resignedCount(0);
    }

    private AppointmentList getExpectedOfficerAppointmentsWithMultipleAppointments() {
        return new AppointmentList()
                .dateOfBirth(dateOfBirth)
                .etag("etag")
                .isCorporateOfficer(false)
                .itemsPerPage(ITEMS_PER_PAGE)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self("/officers/officerId/appointments"))
                .items(getMultipleAppointments())
                .name("forename secondForename surname")
                .startIndex(START_INDEX)
                .activeCount(7)
                .inactiveCount(1)
                .resignedCount(2)
                .totalResults(10);
    }

    private List<OfficerAppointmentSummary> getMultipleAppointments() {
        List<OfficerAppointmentSummary> officerAppointmentSummaryList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            officerAppointmentSummaryList.add(officerAppointmentSummary);
        }
        return officerAppointmentSummaryList;
    }
}