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
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
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
    @DisplayName("Should map officer appointments aggregate to an officer appointments api")
    void map() {
        // given
        when(itemsMapper.map(any())).thenReturn(singletonList(officerAppointmentSummary));
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate(DIRECTOR);
        CompanyAppointmentData companyAppointmentData = getCompanyAppointmentData(getOfficerData(DIRECTOR));
        AppointmentList expected = getExpectedOfficerAppointments(false);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(companyAppointmentData)
                .aggregate(officerAppointmentsAggregate));

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0), DIRECTOR);
        verify(roleMapper).mapIsCorporateOfficer(DIRECTOR);
        verify(itemsMapper).map(singletonList(companyAppointmentData));
        verify(nameMapper).map(getOfficerData(DIRECTOR));
    }

    @Test
    @DisplayName("Should map officer appointments aggregate to an officer appointments api with appointment counts")
    void mapWithAppointmentCounts() {
        // given
        when(itemsMapper.map(any())).thenReturn(getMultipleAppointments());
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);

        AppointmentsCounts counts = new AppointmentsCounts()
                .activeCount(7)
                .inactiveCount(1)
                .resignedCount(2);

        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregateWithMultipleResults();
        CompanyAppointmentData companyAppointmentData = getCompanyAppointmentData(getOfficerData(DIRECTOR));
        List<CompanyAppointmentData> companyAppointmentDataList = getListOfCompanyAppointmentData();
        AppointmentList expected = getExpectedOfficerAppointmentsWithMultipleAppointments();

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointmentsWithCounts(new MapperRequest()
                        .startIndex(START_INDEX)
                        .itemsPerPage(ITEMS_PER_PAGE)
                        .firstAppointment(companyAppointmentData)
                        .aggregate(officerAppointmentsAggregate),
                counts);

        // then
        assertTrue(actual.isPresent());
        assertEquals(7, actual.get().getActiveCount());
        assertEquals(1, actual.get().getInactiveCount());
        assertEquals(2, actual.get().getResignedCount());
        assertEquals(expected, actual.get());

        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0), DIRECTOR);
        verify(roleMapper).mapIsCorporateOfficer(DIRECTOR);
        verify(itemsMapper).map(companyAppointmentDataList);
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
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate(
                CORPORATE_MANAGING_OFFICER);
        CompanyAppointmentData companyAppointmentData = getCompanyAppointmentData(
                getOfficerData(CORPORATE_MANAGING_OFFICER));

        AppointmentList expected = getExpectedOfficerAppointments(true);
        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(companyAppointmentData)
                .aggregate(officerAppointmentsAggregate));

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0), CORPORATE_MANAGING_OFFICER);
        verify(roleMapper).mapIsCorporateOfficer(CORPORATE_MANAGING_OFFICER);
        verify(itemsMapper).map(singletonList(companyAppointmentData));
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
        OfficerAppointmentsAggregate aggregate = new OfficerAppointmentsAggregate();
        aggregate.setTotalResults(0);
        CompanyAppointmentData companyAppointmentData = getCompanyAppointmentData(getOfficerData(DIRECTOR));
        AppointmentList expected = getExpectedOfficerAppointments(false);
        expected.setItems(emptyList());
        expected.setTotalResults(0);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(new MapperRequest()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(companyAppointmentData)
                .aggregate(aggregate));

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0), DIRECTOR);
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
                .firstAppointment(getCompanyAppointmentData(null))
                .aggregate(new OfficerAppointmentsAggregate()));

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(dobMapper);
        verifyNoInteractions(roleMapper);
        verifyNoInteractions(itemsMapper);
        verifyNoInteractions(nameMapper);
    }

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregate(String role) {
        CompanyAppointmentData data = getCompanyAppointmentData(getOfficerData(role));

        OfficerAppointmentsAggregate officerAppointmentsAggregate = new OfficerAppointmentsAggregate();
        officerAppointmentsAggregate.setTotalResults(1);
        officerAppointmentsAggregate.setOfficerAppointments(singletonList(data));
        return officerAppointmentsAggregate;
    }

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregateWithMultipleResults() {
        List<CompanyAppointmentData> data = getListOfCompanyAppointmentData();

        return new OfficerAppointmentsAggregate()
                .setTotalResults(10)
                .setOfficerAppointments(data);
    }

    private List<CompanyAppointmentData> getListOfCompanyAppointmentData() {
        List<CompanyAppointmentData> companyAppointmentDataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            companyAppointmentDataList.add(getCompanyAppointmentData(getOfficerData(
                    OfficerAppointmentsMapperTest.DIRECTOR)));
        }
        return companyAppointmentDataList;
    }

    private CompanyAppointmentData getCompanyAppointmentData(OfficerData officerData) {
        CompanyAppointmentData data = new CompanyAppointmentData();
        data.setOfficerId("officerId");
        data.setId("id");
        data.setCompanyStatus("active");
        data.setData(officerData);
        data.setCompanyName("company name");
        return data;
    }

    private OfficerData getOfficerData(String role) {
        return OfficerData.builder()
                .withEtag("etag")
                .withOfficerRole(role)
                .withTitle("Mrs")
                .withDateOfBirth(LocalDateTime.of(2000, 1, 1, 0, 0))
                .build();
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
                .totalResults(1);
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