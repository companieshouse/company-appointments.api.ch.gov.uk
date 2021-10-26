package uk.gov.companieshouse.company_appointments;

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
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentV2View;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentV2ServiceTest {

    private CompanyAppointmentV2Service companyAppointmentService;

    @Mock
    private CompanyAppointmentV2Repository companyAppointmentRepository;

    @Mock
    private AppointmentApiRepository appointmentApiRepository;

    private AppointmentApiEntity appointmentEntity;

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
        companyAppointmentService = new CompanyAppointmentV2Service(companyAppointmentRepository,
                appointmentApiRepository);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        // given
        appointmentEntity = new AppointmentApiEntity(new AppointmentAPI(
            "id",
            new OfficerAPI(),
            "internalId",
            "appointmentId",
            "officerId",
            "previousOfficerId",
            "companyNumber",
            "deltaAt"));

        when(companyAppointmentRepository.findByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(appointmentEntity));

        // when
        CompanyAppointmentV2View result = companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertThat(result, isA(CompanyAppointmentV2View.class));
        verify(companyAppointmentRepository).findByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testFetchAppointmentThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.findByCompanyNumberAndAppointmentID(any(), any()))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testPutAppointmentData() {
        // given
        appointmentEntity = new AppointmentApiEntity(new AppointmentAPI(
                "id",
                new OfficerAPI(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "companyNumber",
                "deltaAt"));

        // When
        companyAppointmentService.insertAppointmentDelta(appointmentEntity);

        // then
        verify(appointmentApiRepository).insertOrUpdate(appointmentEntity);
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testRejectStaleDelta(
            final String existingDeltaAt,
            final String incomingDeltaAt,
            boolean shouldBeStale) throws Exception {

        // given
        appointmentEntity = new AppointmentApiEntity(new AppointmentAPI(
                "id",
                new OfficerAPI(),
                "internalId",
                "appointmentId",
                "officerId",
                "previousOfficerId",
                "companyNumber",
                incomingDeltaAt));

        when(appointmentApiRepository.existsByIdAndDeltaAtGreaterThanEqual("id", appointmentEntity.getDeltaAt()))
                .thenReturn(existingDeltaAt.compareTo(appointmentEntity.getDeltaAt()) >= 0);

        // When
        companyAppointmentService.insertAppointmentDelta(appointmentEntity);

        // then
        final VerificationMode verificationMode = shouldBeStale ? never() : atLeastOnce();
        verify(appointmentApiRepository, verificationMode).insertOrUpdate(appointmentEntity);
    }
}
