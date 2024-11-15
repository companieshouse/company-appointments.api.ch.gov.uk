package uk.gov.companieshouse.company_appointments.service;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.ConflictException;
import uk.gov.companieshouse.company_appointments.model.DeleteAppointmentParameters;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@ExtendWith(MockitoExtension.class)
class DeleteAppointmentServiceTest {

    private static final DateTimeFormatter DELTA_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);
    private static final String OFFICER_ID = "officer_id";
    private static final String COMPANY_NUMBER = "123456";
    private static final String APPOINTMENT_ID = "345678";
    private static final String REQUEST_DELTA_AT = "20230925171003950844";
    private static final Instant OLDER_DELTA_AT = LocalDateTime.parse("20220924171003950844", DELTA_AT_FORMATTER)
            .toInstant(UTC);
    private static final Instant NEWER_DELTA_AT = LocalDateTime.parse("20240926171003950844", DELTA_AT_FORMATTER)
            .toInstant(UTC);

    @InjectMocks
    private DeleteAppointmentService deleteAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;
    @Mock
    private ResourceChangedApiService resourceChangedApiService;
    @Mock
    private ResourceChangedDataCleaner resourceChangedDataCleaner;

    @Mock
    private CompanyAppointmentDocument companyAppointmentDocument;
    @Mock
    private Object cleanOfficerSummary;

    @Test
    void shouldDeleteAppointment() {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(anyString(), anyString()))
                .thenReturn(Optional.of(companyAppointmentDocument));
        when(companyAppointmentDocument.getDeltaAt()).thenReturn(OLDER_DELTA_AT);
        when(resourceChangedDataCleaner.cleanOutNullValues(any(CompanyAppointmentDocument.class))).thenReturn(
                cleanOfficerSummary);

        ResourceChangedRequest expectedResourceChangeRequest = ResourceChangedRequest.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .officerData(cleanOfficerSummary)
                .delete(true)
                .build();

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(REQUEST_DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // when
        deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verify(companyAppointmentRepository).deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verify(resourceChangedDataCleaner).cleanOutNullValues(companyAppointmentDocument);
        verify(resourceChangedApiService).invokeChsKafkaApi(expectedResourceChangeRequest);
    }

    @Test
    void shouldCallChsKafkaApiWhenNoDocumentInMongoDB() {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(anyString(), anyString())).thenReturn(
                Optional.empty());
        when(resourceChangedDataCleaner.cleanOutNullValues(any(OfficerSummary.class))).thenReturn(cleanOfficerSummary);

        final String appointmentsLink = "/officers/%s/appointments".formatted(OFFICER_ID);
        OfficerSummary expectedOfficerSummary = new OfficerSummary()
                .links(new ItemLinkTypes()
                        .officer(new OfficerLinkTypes()
                                .appointments(appointmentsLink)));

        ResourceChangedRequest expectedResourceChangeRequest = ResourceChangedRequest.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .officerData(cleanOfficerSummary)
                .delete(true)
                .build();

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(REQUEST_DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // when
        deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verifyNoMoreInteractions(companyAppointmentRepository);
        verify(resourceChangedDataCleaner).cleanOutNullValues(expectedOfficerSummary);
        verify(resourceChangedApiService).invokeChsKafkaApi(expectedResourceChangeRequest);
    }

    @Test
    void shouldThrowConflictExceptionWhenRequestIsStale() {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(anyString(), anyString()))
                .thenReturn(Optional.of(companyAppointmentDocument));
        when(companyAppointmentDocument.getDeltaAt()).thenReturn(NEWER_DELTA_AT);

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(REQUEST_DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // when
        Executable executable = () -> deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(ConflictException.class, executable);
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verifyNoMoreInteractions(companyAppointmentRepository);
        verifyNoInteractions(resourceChangedDataCleaner);
        verifyNoInteractions(resourceChangedApiService);
    }

    @Test
    void shouldThrowBadRequestExceptionOnMissingDeltaAt() {
        // given
        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(null)
                .officerId(OFFICER_ID)
                .build();

        // When
        Executable executable = () -> deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(BadRequestException.class, executable);
        verify(companyAppointmentRepository, never()).deleteByCompanyNumberAndID(any(), any());
        verify(resourceChangedApiService, never()).invokeChsKafkaApi(any());
    }

    @ParameterizedTest
    @MethodSource("badGatewayScenarios")
    void shouldThrowBadGatewayExceptionOnMongoReadFailure(NestedRuntimeException exception) {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any())).thenThrow(exception);

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(REQUEST_DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // When
        Executable executable = () -> deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verifyNoMoreInteractions(companyAppointmentRepository);
        verifyNoInteractions(resourceChangedDataCleaner);
        verifyNoInteractions(resourceChangedApiService);
    }

    @ParameterizedTest
    @MethodSource("badGatewayScenarios")
    void shouldThrowBadGatewayExceptionOnMongoWriteFailure(NestedRuntimeException exception) {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(anyString(), anyString()))
                .thenReturn(Optional.of(companyAppointmentDocument));
        when(companyAppointmentDocument.getDeltaAt()).thenReturn(OLDER_DELTA_AT);
        doThrow(exception)
                .when(companyAppointmentRepository).deleteByCompanyNumberAndID(anyString(), anyString());

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(REQUEST_DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // When
        Executable executable = () -> deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verify(companyAppointmentRepository).deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verifyNoInteractions(resourceChangedDataCleaner);
        verifyNoInteractions(resourceChangedApiService);
    }

    private static Stream<Arguments> badGatewayScenarios() {
        return Stream.of(
                Arguments.of(new TransientDataAccessException("...") {
                }),
                Arguments.of(new DataAccessException("...") {
                }));
    }
}
