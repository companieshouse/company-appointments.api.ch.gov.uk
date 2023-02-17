package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
        String role = "director";
        when(itemsMapper.map(any())).thenReturn(singletonList(officerAppointmentSummary));
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(false);
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate(role);
        AppointmentList expected = getExpectedOfficerAppointments(false, OfficerAppointmentSummary.OfficerRoleEnum.DIRECTOR);

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(officerAppointmentsAggregate);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0), role);
        verify(roleMapper).mapIsCorporateOfficer(role);
        verify(itemsMapper).map(singletonList(getCompanyAppointmentData(getOfficerData(role))));
        verify(nameMapper).map(getOfficerData(role));
    }

    @Test
    @DisplayName("Should map corporate managing officer appointments aggregate to an officer appointments api")
    void mapCorporateManagingOfficer() {
        // given
        String role = "corporate-managing-officer";
        when(itemsMapper.map(any())).thenReturn(singletonList(officerAppointmentSummary));
        when(nameMapper.map(any())).thenReturn("forename secondForename surname");
        when(dobMapper.map(any(), anyString())).thenReturn(dateOfBirth);
        when(roleMapper.mapIsCorporateOfficer(anyString())).thenReturn(true);
        OfficerAppointmentsAggregate officerAppointmentsAggregate = getOfficerAppointmentsAggregate(role);

        AppointmentList expected = getExpectedOfficerAppointments(true, OfficerAppointmentSummary.OfficerRoleEnum.CORPORATE_MANAGING_OFFICER);
        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(officerAppointmentsAggregate);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(dobMapper).map(LocalDateTime.of(2000, 1, 1, 0, 0), role);
        verify(roleMapper).mapIsCorporateOfficer(role);
        verify(itemsMapper).map(singletonList(getCompanyAppointmentData(getOfficerData(role))));
        verify(nameMapper).map(getOfficerData(role));
    }

    @Test
    @DisplayName("Should return an empty optional if the list of appointments within the aggregate is empty")
    void mapEmptyAppointmentsList() {
        // given
        OfficerAppointmentsAggregate aggregate = new OfficerAppointmentsAggregate();

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(aggregate);

        // then
        assertFalse(actual.isPresent());
        verifyNoInteractions(dobMapper);
        verifyNoInteractions(roleMapper);
        verifyNoInteractions(itemsMapper);
        verifyNoInteractions(nameMapper);
    }

    @Test
    @DisplayName("Should return an empty optional if the list of appointments within the aggregate has null officer data")
    void mapNullOfficerData() {
        // given
        OfficerAppointmentsAggregate aggregate = new OfficerAppointmentsAggregate();
        aggregate.getOfficerAppointments().add(new CompanyAppointmentData());

        // when
        Optional<AppointmentList> actual = mapper.mapOfficerAppointments(aggregate);

        // then
        assertFalse(actual.isPresent());
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

    private AppointmentList getExpectedOfficerAppointments(boolean isCorporateOfficer, OfficerAppointmentSummary.OfficerRoleEnum role) {
        return new AppointmentList()
                .dateOfBirth(dateOfBirth)
                .etag("etag")
                .isCorporateOfficer(isCorporateOfficer)
                .itemsPerPage(35)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self("/officers/officerId/appointments"))
                .items(singletonList(officerAppointmentSummary))
                .name("forename secondForename surname")
                .startIndex(0)
                .totalResults(1);
    }
}