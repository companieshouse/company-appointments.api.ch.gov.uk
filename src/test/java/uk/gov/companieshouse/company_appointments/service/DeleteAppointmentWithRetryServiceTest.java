package uk.gov.companieshouse.company_appointments.service;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.DeleteAppointmentParameters;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@ExtendWith(MockitoExtension.class)
class DeleteAppointmentWithRetryServiceTest {

    private static final DateTimeFormatter DELTA_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);
    private static final String OFFICER_ID = "officer_id";
    private static final String COMPANY_NUMBER = "123456";
    private static final String APPOINTMENT_ID = "345678";
    private static final String DELTA_AT = "20140925171003950844";
    private static final Instant OLDER_DELTA_AT = LocalDateTime.parse("20140924171003950844", DELTA_AT_FORMATTER).toInstant(UTC);
    private static final Instant NEWER_DELTA_AT = LocalDateTime.parse("20140926171003950844", DELTA_AT_FORMATTER).toInstant(UTC);

    private DeleteAppointmentService deleteAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;
    @Mock
    private ResourceChangedApiService resourceChangedApiService;
    @Mock
    private CompanyAppointmentMapper companyAppointmentMapper;
    @Captor
    private ArgumentCaptor<ResourceChangedRequest> resourceChangedRequestArgumentCaptor;

    @BeforeEach
    void setUp() {
        deleteAppointmentService =
                new DeleteAppointmentService(
                        companyAppointmentRepository,
                        resourceChangedApiService,
                        companyAppointmentMapper);
    }

    @Test
    void deleteOfficer() {
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(new CompanyAppointmentDocument()
                        .deltaAt(OLDER_DELTA_AT)));

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        verify(companyAppointmentRepository).deleteByCompanyNumberAndID(COMPANY_NUMBER,
                APPOINTMENT_ID);
        verify(resourceChangedApiService).invokeChsKafkaApi(resourceChangedRequestArgumentCaptor.capture());
        assertNotNull(resourceChangedRequestArgumentCaptor.getValue());
    }

    @Test
    void shouldIgnoreStaleDeleteDelta() {
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(new CompanyAppointmentDocument()
                        .deltaAt(NEWER_DELTA_AT)));

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        verify(companyAppointmentRepository, never()).deleteByCompanyNumberAndID(any(), any());
        verify(resourceChangedApiService, never()).invokeChsKafkaApi(any());
    }

    @Test
    void shouldNotifyRetriedDelta() {
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.empty());

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        verify(companyAppointmentRepository, never()).deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
        verify(resourceChangedApiService).invokeChsKafkaApi(resourceChangedRequestArgumentCaptor.capture());
        assertNotNull(resourceChangedRequestArgumentCaptor.getValue());
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

    @Test
    void shouldThrowServiceUnavailableExceptionOnMongoReadFailure() {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any()))
                .thenThrow(new DataAccessException("...") {
                });

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // When
        Executable executable = () -> deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(resourceChangedApiService, never()).invokeChsKafkaApi(any());
    }

    @Test
    void shouldThrowServiceUnavailableExceptionOnKafkaNotificationFailure()
            throws ServiceUnavailableException {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(new CompanyAppointmentDocument()
                        .deltaAt(OLDER_DELTA_AT)));
        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(IllegalArgumentException.class);

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .companyNumber(COMPANY_NUMBER)
                .appointmentId(APPOINTMENT_ID)
                .deltaAt(DELTA_AT)
                .officerId(OFFICER_ID)
                .build();

        // When
        Executable executable = () -> deleteAppointmentService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }
}
