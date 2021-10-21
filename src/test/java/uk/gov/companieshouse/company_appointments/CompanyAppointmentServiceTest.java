package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerLinksAPI;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    @Mock
    private AppointmentApiRepository appointmentApiRepository;

    @Mock
    private CompanyAppointmentMapper companyAppointmentMapper;

    @Mock
    private CompanyAppointmentData companyAppointmentData;

    @Mock
    private CompanyAppointmentView companyAppointmentView;

    private AppointmentAPI appointment;

    private final static String COMPANY_NUMBER = "123456";

    private final static String APPOINTMENT_ID = "345678";

    private static Stream<Arguments> deltaAtTestCases() {
        return Stream.of(
                // exitingDelta, incomingDelta, shouldBeStale
                Arguments.of("1", "2", false), // 1 < 2 so delta should not be stale
                Arguments.of("11", "1", true), // shorter string is considered less than
                Arguments.of("2", "1", true), // 2 < 1 so delta should be stale
                Arguments.of("1", "1", true), // 1 == 1 so delta should be stale
                Arguments.of("20140925171003950844", "20140925171003950845", false), // Newer timestamp not stale
                Arguments.of("20140925171003950844", "20140925171003950843", true) // Older timestamp stale
        );
    }

    @BeforeEach
    void setUp() {
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository,
                appointmentApiRepository, companyAppointmentMapper);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(companyAppointmentData));

        when(companyAppointmentMapper.map(companyAppointmentData)).thenReturn(companyAppointmentView);

        // when
        CompanyAppointmentView result = companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(companyAppointmentView, result);
        verify(companyAppointmentRepository).readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID);
        verify(companyAppointmentMapper).map(companyAppointmentData);
    }

    @Test
    void testFetchAppointmentThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(any(), any()))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testPutAppointmentData() {
        // given
        appointment = new AppointmentAPI(
                "id",
                new OfficerAPI(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "deltaAt");

        // When
        companyAppointmentService.insertAppointmentDelta(appointment);

        // then
        verify(appointmentApiRepository).insertOrUpdate(appointment);
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testRejectStaleDelta(
            final String existingDeltaAt,
            final String incomingDeltaAt,
            boolean shouldBeStale) throws Exception {

        // given
        appointment = new AppointmentAPI(
                "id",
                new OfficerAPI(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                incomingDeltaAt);

        when(appointmentApiRepository.existsByIdAndDeltaAtGreaterThanEqual("id", appointment.getDeltaAt()))
                .thenReturn(existingDeltaAt.compareTo(appointment.getDeltaAt()) >= 0);

        // When
        companyAppointmentService.insertAppointmentDelta(appointment);

        // then
        final VerificationMode verificationMode = shouldBeStale ? never() : atLeastOnce();
        verify(appointmentApiRepository, verificationMode).insertOrUpdate(appointment);
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

        appointment = spy(new AppointmentAPI(
                "id",
                officer,
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "deltaAt"));

        // When
        companyAppointmentService.insertAppointmentDelta(appointment);

        // then
        verify(officer).setAdditionalProperties(null);
        verify(serviceAddress).setAdditionalProperties(null);
        verify(ura).setAdditionalProperties(null);
        verify(formerName).setAdditionalProperties(null);
        verify(identificationAPI).setAdditionalProperties(null);
        verify(linksAPI).setAdditionalProperties(null);
        verify(officerLinksAPI).setAdditionalProperties(null);
    }
}
