package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

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
}
