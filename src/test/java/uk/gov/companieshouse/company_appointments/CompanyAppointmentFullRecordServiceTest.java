package uk.gov.companieshouse.company_appointments;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerLinksAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordServiceTest {

    private CompanyAppointmentFullRecordService companyAppointmentService;

    @Mock
    private CompanyAppointmentFullRecordRepository companyAppointmentRepository;

    @Captor
    private ArgumentCaptor<AppointmentApiEntity> captor;

    private AppointmentApiEntity appointmentEntity;

    private final static String COMPANY_NUMBER = "123456";

    private final static String APPOINTMENT_ID = "345678";

    private final static Instant CREATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");

    private final static InstantAPI instantAPI = new InstantAPI(CREATED_AT);

    private final static Clock CLOCK = Clock.fixed(CREATED_AT, ZoneId.of("UTC"));

    private static Stream<Arguments> deltaAtTestCases() {
        return Stream.of(
                // exitingDelta, incomingDelta, deltaExists, shouldBeStale
                Arguments.of("1", "2", false, false), // delta does not exist
                Arguments.of("1", "2", true, false), // 1 < 2 so delta should not be stale
                Arguments.of("11", "1", true, true), // shorter string is considered less than
                Arguments.of("2", "1", true, true), // 2 < 1 so delta should be stale
                Arguments.of("1", "1", true, true), // 1 == 1 so delta should be stale
                Arguments.of("20140925171003950844", "20140925171003950845", true, false), // Newer timestamp not stale
                Arguments.of("20140925171003950844", "20140925171003950843", true, true) // Older timestamp stale
        );
    }

    @BeforeEach
    void setUp() {
        companyAppointmentService =
                new CompanyAppointmentFullRecordService(companyAppointmentRepository, CLOCK);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        // given
        appointmentEntity = new AppointmentApiEntity(
                new AppointmentAPI("id", new OfficerAPI(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", instantAPI, "deltaAt"));

        when(companyAppointmentRepository.findByCompanyNumberAndAppointmentID(COMPANY_NUMBER,
                APPOINTMENT_ID)).thenReturn(Optional.of(appointmentEntity));

        // when
        CompanyAppointmentFullRecordView result =
                companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertThat(result, isA(CompanyAppointmentFullRecordView.class));
        verify(companyAppointmentRepository).findByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testFetchAppointmentThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.findByCompanyNumberAndAppointmentID(any(), any()))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    @ParameterizedTest
    @ValueSource(booleans={true, false})
    void testPutAppointmentData(boolean deltaExists) {
        // given
        appointmentEntity = new AppointmentApiEntity(
                new AppointmentAPI("id", new OfficerAPI(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", null, "deltaAt"));
        when(companyAppointmentRepository.existsByIdAndCompanyNumber("id", "companyNumber")).thenReturn(deltaExists);

        // When
        companyAppointmentService.insertAppointmentDelta(appointmentEntity);

        // then
        verify(companyAppointmentRepository).insertOrUpdate(captor.capture());
        assertThat(captor.getValue().getCreated(), is(deltaExists ? nullValue() : equalTo(instantAPI)));
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testRejectStaleDelta(
            final String existingDeltaAt,
            final String incomingDeltaAt,
            boolean deltaExists,
            boolean shouldBeStale) {

        // given
        appointmentEntity = new AppointmentApiEntity(
                new AppointmentAPI("id", new OfficerAPI(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", instantAPI, incomingDeltaAt));
        when(companyAppointmentRepository.existsByIdAndCompanyNumber(
            appointmentEntity.getId(), appointmentEntity.getCompanyNumber())).thenReturn(deltaExists);
        if (deltaExists) {
            when(companyAppointmentRepository.existsByIdAndDeltaAtGreaterThanEqual(
                appointmentEntity.getId(), incomingDeltaAt)).thenReturn(incomingDeltaAt.compareTo(existingDeltaAt) <= 0);
        }

        // When
        companyAppointmentService.insertAppointmentDelta(appointmentEntity);

        // then
        VerificationMode expectedTimes = (deltaExists && shouldBeStale) ? never() : times(1);
        verify(companyAppointmentRepository, expectedTimes).insertOrUpdate(appointmentEntity);
    }

    @Test
    @DisplayName("Tests if the additionalProperties field is set to null to prevent being stored")
    void additionalPropertiesRemoved() {
        // given
        final OfficerAPI officer = spy(OfficerAPI.class);

        final AddressAPI serviceAddress = spy(AddressAPI.class);
        when(officer.getServiceAddress()).thenReturn(serviceAddress);

        final AddressAPI ura = spy(AddressAPI.class);
        when(officer.getUsualResidentialAddress()).thenReturn(ura);

        final FormerNamesAPI formerName = spy(FormerNamesAPI.class);
        final List<FormerNamesAPI> formerNames = new ArrayList<>();
        formerNames.add(formerName);
        when(officer.getFormerNameData()).thenReturn(formerNames);

        final IdentificationAPI identificationAPI = spy(IdentificationAPI.class);
        when(officer.getIdentificationData()).thenReturn(identificationAPI);

        final LinksAPI linksAPI = spy(LinksAPI.class);
        when(officer.getLinksData()).thenReturn(linksAPI);

        final OfficerLinksAPI officerLinksAPI = spy(OfficerLinksAPI.class);
        when(linksAPI.getOfficerLinksData()).thenReturn(officerLinksAPI);

        appointmentEntity = spy(new AppointmentApiEntity(
                new AppointmentAPI("id", officer, "internalId", "appointmentId", "officerId", "previousOfficerId",
                        "companyNumber", instantAPI, "deltaAt")));

        // When
        companyAppointmentService.insertAppointmentDelta(appointmentEntity);

        // then
        verify(officer).setAdditionalProperties(null);
        verify(serviceAddress).setAdditionalProperties(null);
        verify(ura).setAdditionalProperties(null);
        verify(formerName).setAdditionalProperties(null);
        verify(identificationAPI).setAdditionalProperties(null);
        verify(linksAPI).setAdditionalProperties(null);
        verify(officerLinksAPI).setAdditionalProperties(null);
    }

    @Test
    @DisplayName("Tests if the additionalProperties removal skipped for null officer")
    void additionalPropertiesSkippedWhenOfficerNull() {
        // given
        final OfficerAPI officer = null;

        appointmentEntity = spy(new AppointmentApiEntity(
                new AppointmentAPI("id", officer, "internalId", "appointmentId", "officerId", "previousOfficerId",
                        "companyNumber", instantAPI, "deltaAt")));

        // When
        companyAppointmentService.insertAppointmentDelta(appointmentEntity);

        // then
        verify(companyAppointmentRepository).insertOrUpdate(appointmentEntity);
    }

}
