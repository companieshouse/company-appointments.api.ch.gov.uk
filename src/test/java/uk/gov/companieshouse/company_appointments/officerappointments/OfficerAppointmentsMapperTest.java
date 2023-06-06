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
    @Mock
    private AppointmentCounts appointmentCounts;

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
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(START_INDEX, ITEMS_PER_PAGE, companyAppointmentData, officerAppointmentsAggregate, null);

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
        when(itemsMapper.map(any())).thenReturn(getMultipleAppointments(10));
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);
        when(appointmentCounts.getInactiveCount()).thenReturn(1);
        when(appointmentCounts.getResignedCount()).thenReturn(2);

        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregateWithMultipleResults(DIRECTOR, 10);
        CompanyAppointmentData companyAppointmentData = getCompanyAppointmentData(getOfficerData(DIRECTOR));
        List<CompanyAppointmentData> companyAppointmentDataList = getListOfCompanyAppointmentData(DIRECTOR, 10);
        AppointmentList expected = getExpectedOfficerAppointmentsWithMultipleAppointments(false, 10);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(START_INDEX, ITEMS_PER_PAGE, companyAppointmentData, officerAppointmentsAggregate, appointmentCounts);

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
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate(CORPORATE_MANAGING_OFFICER);
        CompanyAppointmentData companyAppointmentData = getCompanyAppointmentData(getOfficerData(CORPORATE_MANAGING_OFFICER));

        AppointmentList expected = getExpectedOfficerAppointments(true);
        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(START_INDEX, ITEMS_PER_PAGE, companyAppointmentData, officerAppointmentsAggregate, null);

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
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(START_INDEX, ITEMS_PER_PAGE, companyAppointmentData, aggregate, null);

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
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(
                START_INDEX,
                ITEMS_PER_PAGE,
                getCompanyAppointmentData(null),
                new OfficerAppointmentsAggregate(),
                null);

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

    private OfficerAppointmentsAggregate getOfficerAppointmentsAggregateWithMultipleResults(String role, int numberOfResults) {
        List<CompanyAppointmentData> data = getListOfCompanyAppointmentData(role, numberOfResults);

        return new OfficerAppointmentsAggregate()
                    .setTotalResults(numberOfResults)
                    .setOfficerAppointments(data);
    }

    private List<CompanyAppointmentData> getListOfCompanyAppointmentData(String role, int numberOfElements) {
        List<CompanyAppointmentData> companyAppointmentDataList = new ArrayList<>();
        for (int i = 0; i < numberOfElements; i++) {
            companyAppointmentDataList.add(getCompanyAppointmentData(getOfficerData(role)));
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

    private AppointmentList getExpectedOfficerAppointmentsWithMultipleAppointments(boolean isCorporateOfficer, int numberOfAppointments) {
        return new AppointmentList()
                .dateOfBirth(dateOfBirth)
                .etag("etag")
                .isCorporateOfficer(isCorporateOfficer)
                .itemsPerPage(ITEMS_PER_PAGE)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self("/officers/officerId/appointments"))
                .items(getMultipleAppointments(numberOfAppointments))
                .name("forename secondForename surname")
                .startIndex(START_INDEX)
                .activeCount(7)
                .inactiveCount(1)
                .resignedCount(2)
                .totalResults(numberOfAppointments);
    }

    private List<OfficerAppointmentSummary> getMultipleAppointments(int numberOfAppointments) {
        List<OfficerAppointmentSummary> officerAppointmentSummaryList = new ArrayList<>();
        for(int i = 0; i < numberOfAppointments; i++) {
            officerAppointmentSummaryList.add(officerAppointmentSummary);
        }
        return officerAppointmentSummaryList;
    }
}