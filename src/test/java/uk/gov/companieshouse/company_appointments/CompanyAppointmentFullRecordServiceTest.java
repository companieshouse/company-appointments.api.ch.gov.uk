package uk.gov.companieshouse.company_appointments;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerLinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
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

    @Mock
    private AppointmentApiEntity appointmentApiEntity;

    @Captor
    private ArgumentCaptor<DeltaAppointmentApiEntity> captor;

    private AppointmentApiEntity appointmentApi;

    @Mock
    private DeltaAppointmentApiEntity deltaAppointmentApiEntity;

    private DeltaAppointmentApi deltaAppointmentApi;

    private FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi;

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
        deltaAppointmentApi = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", new Data(), new SensitiveData(), new InternalData(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", instantAPI, instantAPI, "deltaAt", 22));

        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(Optional.of(deltaAppointmentApi));

        // when
        CompanyAppointmentFullRecordView result =
                companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertThat(result, isA(CompanyAppointmentFullRecordView.class));
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testFetchAppointmentThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any()))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testPutAppointmentData() {
        // given
        deltaAppointmentApi = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", new Data(), new SensitiveData(), new InternalData(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", null, null, "deltaAt", 22));
        when(companyAppointmentRepository.readByCompanyNumberAndID(deltaAppointmentApi.getCompanyNumber(),
                deltaAppointmentApi.getId())).thenReturn(Optional.of(deltaAppointmentApiEntity));

        // When
        companyAppointmentService.insertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).insertOrUpdate(captor.capture());
        assertNotNull(captor.getValue().getEtag());
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testRejectStaleDelta(
            final String existingDeltaAt,
            final String incomingDeltaAt,
            boolean deltaExists,
            boolean shouldBeStale) {

        // given
        deltaAppointmentApi = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", new Data(), new SensitiveData(), new InternalData(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", null, null, "deltaAt", 22));

        DeltaAppointmentApiEntity appointmentEntity = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", new Data(), new SensitiveData(), new InternalData(), "internalId", "appointmentId", "officerId",
                        "previousOfficerId", "companyNumber", null, null, "deltaAt", 22));

        when(companyAppointmentRepository.readByCompanyNumberAndID(appointmentApi.getCompanyNumber(),
                appointmentApi.getId())).thenReturn(deltaExists ? Optional.of(appointmentEntity) : Optional.empty());

        // When
        companyAppointmentService.insertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        VerificationMode expectedTimes = (deltaExists && shouldBeStale) ? never() : times(1);
        verify(companyAppointmentRepository, expectedTimes).insertOrUpdate(deltaAppointmentApi);
    }

//    @Test
//    @DisplayName("Tests if the additionalProperties field is set to null to prevent being stored")
//    void additionalPropertiesRemoved() {
//        // given
//        final OfficerAPI officer = spy(OfficerAPI.class);
//        final SensitiveOfficerAPI sensitiveOfficer = spy(SensitiveOfficerAPI.class);
//
//        final AddressAPI serviceAddress = spy(AddressAPI.class);
//        when(officer.getServiceAddress()).thenReturn(serviceAddress);
//
//        final FormerNamesAPI formerName = spy(FormerNamesAPI.class);
//        final List<FormerNamesAPI> formerNames = new ArrayList<>();
//        formerNames.add(formerName);
//        when(officer.getFormerNameData()).thenReturn(formerNames);
//
//        final IdentificationAPI identificationAPI = spy(IdentificationAPI.class);
//        when(officer.getIdentificationData()).thenReturn(identificationAPI);
//
//        final LinksAPI linksAPI = spy(LinksAPI.class);
//        when(officer.getLinksData()).thenReturn(linksAPI);
//
//        final OfficerLinksAPI officerLinksAPI = spy(OfficerLinksAPI.class);
//        when(linksAPI.getOfficerLinksData()).thenReturn(officerLinksAPI);
//
//        appointmentApi = spy(new AppointmentApiEntity(
//                new AppointmentAPI("id", officer, sensitiveOfficer, "internalId", "appointmentId", "officerId", "previousOfficerId",
//                        "companyNumber", instantAPI, instantAPI, "deltaAt", 22)));
//
//        // When
//        companyAppointmentService.insertAppointmentDelta(appointmentApi);
//
//        // then
//        verify(officer).setAdditionalProperties(null);
//        verify(serviceAddress).setAdditionalProperties(null);
//        verify(formerName).setAdditionalProperties(null);
//        verify(identificationAPI).setAdditionalProperties(null);
//        verify(linksAPI).setAdditionalProperties(null);
//        verify(officerLinksAPI).setAdditionalProperties(null);
//    }

//    @Test
//    @DisplayName("Tests if the additionalProperties removal skipped for null officer")
//    void additionalPropertiesSkippedWhenOfficerNull() {
//        // given
//        final OfficerAPI officer = null;
//        final SensitiveOfficerAPI sensitiveOfficer = null;
//
//        appointmentApi = spy(new AppointmentApiEntity(
//                new AppointmentAPI("id", officer, sensitiveOfficer, "internalId", "appointmentId", "officerId", "previousOfficerId",
//                        "companyNumber", instantAPI, instantAPI, "deltaAt", 22)));
//
//        // When
//        companyAppointmentService.insertAppointmentDelta(appointmentApi);
//
//        // then
//        verify(companyAppointmentRepository).insertOrUpdate(appointmentApi);
//    }

    @Test
    void deleteOfficer() throws Exception {
        when(companyAppointmentRepository.deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(deltaAppointmentApiEntity));

        companyAppointmentService.deleteOfficer(COMPANY_NUMBER, APPOINTMENT_ID);

        verify(companyAppointmentRepository).deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void deleteOfficerThrowsNotFound() {
        when(companyAppointmentRepository.deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.deleteOfficer(COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

}
