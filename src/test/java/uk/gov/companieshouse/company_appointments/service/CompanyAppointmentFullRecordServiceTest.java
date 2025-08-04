package uk.gov.companieshouse.company_appointments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.api.OfficerMergeClient;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.ConflictException;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirthView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordServiceTest {

    private CompanyAppointmentFullRecordService companyAppointmentService;

    @Mock
    private DeltaAppointmentTransformer deltaAppointmentTransformer;
    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;
    @Mock
    private ResourceChangedApiService resourceChangedApiService;
    @Mock
    private CompanyAppointmentMapper companyAppointmentMapper;
    @Mock
    private OfficerMergeClient officerMergeClient;
    @Captor
    private ArgumentCaptor<CompanyAppointmentDocument> captor;

    private final FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = buildFullRecordOfficer();

    private static final String COMPANY_NUMBER = "123456";
    private static final String APPOINTMENT_ID = "345678";
    private static final OffsetDateTime DELTA_AT_LATER = OffsetDateTime.parse("2022-01-14T00:00:00.000000Z");
    private static final OffsetDateTime DELTA_AT_STALE = OffsetDateTime.parse("2022-01-12T00:00:00.000000Z");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2021-08-01T00:00:00.000000Z"),
            ZoneId.of("UTC"));

    private static Stream<Arguments> deltaAtTestCases() {
        return Stream.of(
                // existingDelta, incomingDelta, deltaExists
                Arguments.of("2022-01-13T00:00:00.000000Z", DELTA_AT_LATER, false),
                // delta does not exist
                Arguments.of("2022-01-13T00:00:00.000000Z", DELTA_AT_LATER, true),
                // Newer timestamp not stale
                Arguments.of("2022-01-12T00:00:00.000000Z", DELTA_AT_STALE, true)
                // 1 == 1 so delta should be stale
        );
    }

    @BeforeEach
    void setUp() {
        companyAppointmentService =
                new CompanyAppointmentFullRecordService(deltaAppointmentTransformer,
                        companyAppointmentRepository, resourceChangedApiService, CLOCK, officerMergeClient);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        // given
        DeltaOfficerData data = DeltaOfficerData.Builder.builder()
                .officerRole("director")
                .links(new DeltaItemLinkTypes()
                        .setSelf("self")
                        .setOfficer(new DeltaOfficerLinkTypes()
                                .setSelf("self")))
                .build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData()
                .setDateOfBirth(Instant.parse("1990-01-12T01:02:30.456789Z"));
        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                Instant.parse("2022-01-12T00:00:00.000000Z"), data, sensitiveData);

        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER,
                APPOINTMENT_ID)).thenReturn(Optional.of(deltaAppointmentDocument));

        // when
        CompanyAppointmentFullRecordView result =
                companyAppointmentService.getAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(result.getDateOfBirth(), new DateOfBirthView(12, 1, 1990));
        verify(companyAppointmentRepository).readByCompanyNumberAndID(COMPANY_NUMBER,
                APPOINTMENT_ID);
    }

    @Test
    void testFetchAppointmentThrowsNotFoundExceptionIfAppointmentDoesntExist() {
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any()))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.getAppointment(COMPANY_NUMBER,
                APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    @Test
    void testPutAppointmentData() {
        // given
        DeltaOfficerData data = DeltaOfficerData.Builder.builder()
                .officerRole("director")
                .etag("etag")
                .build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData()
                .setDateOfBirth(Instant.parse("1990-01-12T01:02:30.456789Z"));
        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                Instant.parse("2022-01-12T00:00:00.000000Z"), data, sensitiveData);
        CompanyAppointmentDocument transformedAppointmentApi = builtDeltaAppointmentApi(
                data, sensitiveData, Instant.parse("2022-01-13T00:00:00.000000Z"));

        when(companyAppointmentRepository.readByCompanyNumberAndID(
                transformedAppointmentApi.getCompanyNumber(),
                transformedAppointmentApi.getId())).thenReturn(
                Optional.of(deltaAppointmentDocument));
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenReturn(transformedAppointmentApi);

        // When
        companyAppointmentService.upsertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).save(captor.capture());
        assertNotNull(captor.getValue().getData().getEtag());
    }

    @Test
    void testPutAppointmentDataThrowsServiceUnavailableException() {
        // given
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(
                new CompanyAppointmentDocument());
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(),
                any())).thenThrow(new DataAccessException("...") {
        });

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(
                fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void testPutAppointmentDataThrowsServiceUnavailableExceptionWhenIllegalArgumentExceptionCaught() {
        // given
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(
                new CompanyAppointmentDocument());
        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(IllegalArgumentException.class);

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(
                fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void testInsertAppointmentEvenWhenServiceUnavailableThrown() {
        // given
        CompanyAppointmentDocument deltaAppointmentDocument = new CompanyAppointmentDocument()
                .id("appointmentId")
                .companyNumber("012345678");
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(
                deltaAppointmentDocument);
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any())).thenReturn(Optional.empty());
        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(ServiceUnavailableException.class);

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(
                fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(companyAppointmentRepository).save(deltaAppointmentDocument);
    }

    @Test
    void testUpdateAppointmentEvenWhenServiceUnavailableThrown() {
        // given
        CompanyAppointmentDocument deltaAppointmentDocument = new CompanyAppointmentDocument()
                .id("appointmentId")
                .companyNumber("012345678")
                .deltaAt(Instant.parse("2023-11-06T16:30:00.000000Z"));

        CompanyAppointmentDocument existingDocument = new CompanyAppointmentDocument()
                .id("appointmentId")
                .companyNumber("012345678")
                .deltaAt(Instant.parse("2023-11-06T12:00:00.000000Z"));

        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(
                deltaAppointmentDocument);

        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any())).thenReturn(Optional.of(existingDocument));
        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(ServiceUnavailableException.class);

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(
                fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(companyAppointmentRepository).save(deltaAppointmentDocument);
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testValidDeltaAt(
            final Instant existingDeltaAt,
            final OffsetDateTime incomingDeltaAt,
            boolean deltaExists) {

        // given
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(incomingDeltaAt);

        DeltaOfficerData data = DeltaOfficerData.Builder.builder().build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData();
        String expectedCompanyNumber = "companyNumber";
        String expectedId = "id";

        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                existingDeltaAt, data, sensitiveData);
        CompanyAppointmentDocument transformedAppointmentApi = builtDeltaAppointmentApi(
                data, sensitiveData, incomingDeltaAt.toInstant());

        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenReturn(transformedAppointmentApi);

        when(companyAppointmentRepository.readByCompanyNumberAndID(expectedCompanyNumber,
                expectedId)).thenReturn(deltaExists ? Optional.of(deltaAppointmentDocument)
                : Optional.empty());

        // When
        companyAppointmentService.upsertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).save(any(CompanyAppointmentDocument.class));
    }

    @Test
    void testRejectStaleDelta() {
        // given
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(DELTA_AT_STALE);

        DeltaOfficerData data = DeltaOfficerData.Builder.builder().build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData();
        String expectedCompanyNumber = "companyNumber";
        String expectedId = "id";

        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                Instant.parse("2022-01-13T00:00:00.000000Z"), data, sensitiveData);
        CompanyAppointmentDocument transformedAppointmentApi = builtDeltaAppointmentApi(
                data, sensitiveData, DELTA_AT_STALE.toInstant());

        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenReturn(transformedAppointmentApi);

        when(companyAppointmentRepository.readByCompanyNumberAndID(expectedCompanyNumber,
                expectedId)).thenReturn(Optional.of(deltaAppointmentDocument));

        // when
        Executable actual = () -> companyAppointmentService.upsertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        assertThrows(ConflictException.class, actual);
        verify(companyAppointmentRepository, times(0)).save(
                any(CompanyAppointmentDocument.class));
    }

    @Test
    void testServiceThrows500WhenTransformFails() {
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenThrow(new FailedToTransformException("message"));

        Assert.assertThrows(ServiceUnavailableException.class, () ->
                companyAppointmentService.upsertAppointmentDelta(new FullRecordCompanyOfficerApi()));
    }

    @Test
    void testOfficerMergeInvokedOnMismatchInOfficerIdsBetweenDeltaAndMongo() {
        // given
        DeltaOfficerData data = DeltaOfficerData.Builder.builder()
                .officerRole("director")
                .etag("etag")
                .build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData()
                .setDateOfBirth(Instant.parse("1990-01-12T01:02:30.456789Z"));
        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                Instant.parse("2022-01-12T00:00:00.000000Z"), data, sensitiveData);
        deltaAppointmentDocument.officerId("oldOfficerId");
        CompanyAppointmentDocument transformedAppointmentApi = builtDeltaAppointmentApi(
                data, sensitiveData, Instant.parse("2022-01-13T00:00:00.000000Z"));

        when(companyAppointmentRepository.readByCompanyNumberAndID(
                transformedAppointmentApi.getCompanyNumber(),
                transformedAppointmentApi.getId())).thenReturn(
                Optional.of(deltaAppointmentDocument));
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenReturn(transformedAppointmentApi);

        // When
        companyAppointmentService.upsertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).save(captor.capture());
        verify(officerMergeClient).invokeOfficerMerge("officerId", "oldOfficerId");
        assertNotNull(captor.getValue().getData().getEtag());
    }

    @Test
    void testOfficerMergeInvokedOnMismatchInOfficerIdAndPreviousOfficerIdInDelta() {
        // given
        DeltaOfficerData data = DeltaOfficerData.Builder.builder()
                .officerRole("director")
                .etag("etag")
                .build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData()
                .setDateOfBirth(Instant.parse("1990-01-12T01:02:30.456789Z"));
        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                Instant.parse("2022-01-12T00:00:00.000000Z"), data, sensitiveData);
        CompanyAppointmentDocument transformedAppointmentApi = builtDeltaAppointmentApi(
                data, sensitiveData, Instant.parse("2022-01-13T00:00:00.000000Z"));
        transformedAppointmentApi.previousOfficerId("oldOfficerId");

        when(companyAppointmentRepository.readByCompanyNumberAndID(
                transformedAppointmentApi.getCompanyNumber(),
                transformedAppointmentApi.getId())).thenReturn(
                Optional.of(deltaAppointmentDocument));
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenReturn(transformedAppointmentApi);

        // When
        companyAppointmentService.upsertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).save(captor.capture());
        verify(officerMergeClient).invokeOfficerMerge("officerId", "oldOfficerId");
        assertNotNull(captor.getValue().getData().getEtag());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "null"
    }, nullValues = "null")
    void testEmptyPreviousOfficerIdInDeltaProcessesWithoutOfficerMerge(String previousOfficerId) {
        // given
        DeltaOfficerData data = DeltaOfficerData.Builder.builder()
                .officerRole("director")
                .etag("etag")
                .build();
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData()
                .setDateOfBirth(Instant.parse("1990-01-12T01:02:30.456789Z"));
        CompanyAppointmentDocument deltaAppointmentDocument = buildDeltaAppointmentDocument(
                Instant.parse("2022-01-12T00:00:00.000000Z"), data, sensitiveData);
        CompanyAppointmentDocument transformedAppointmentApi = builtDeltaAppointmentApi(
                data, sensitiveData, Instant.parse("2022-01-13T00:00:00.000000Z"));
        transformedAppointmentApi.previousOfficerId(previousOfficerId);

        when(companyAppointmentRepository.readByCompanyNumberAndID(
                transformedAppointmentApi.getCompanyNumber(),
                transformedAppointmentApi.getId())).thenReturn(
                Optional.of(deltaAppointmentDocument));
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenReturn(transformedAppointmentApi);

        // When
        companyAppointmentService.upsertAppointmentDelta(fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).save(captor.capture());
        verifyNoInteractions(officerMergeClient);
        assertNotNull(captor.getValue().getData().getEtag());
    }

    // TODO: Have the officer merge invocation fail and throw error
    @Test
    void testUpdateAppointmentWhenOfficerMergeFails() {
        // given
        CompanyAppointmentDocument deltaAppointmentDocument = new CompanyAppointmentDocument()
                .id("appointmentId")
                .companyNumber("012345678")
                .deltaAt(Instant.parse("2023-11-06T16:30:00.000000Z"));

        CompanyAppointmentDocument existingDocument = new CompanyAppointmentDocument()
                .id("appointmentId")
                .companyNumber("012345678")
                .deltaAt(Instant.parse("2023-11-06T12:00:00.000000Z"));

        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(
                deltaAppointmentDocument);

        when(companyAppointmentRepository.readByCompanyNumberAndID(any(), any())).thenReturn(Optional.of(existingDocument));
        //when(officerMergeClient.invokeOfficerMerge(anyString(), anyString())).thenThrow(ServiceUnavailableException.class);

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(
                fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(companyAppointmentRepository).save(deltaAppointmentDocument);
    }

    private FullRecordCompanyOfficerApi buildFullRecordOfficer() {
        FullRecordCompanyOfficerApi output = new FullRecordCompanyOfficerApi();

        ExternalData externalData = new ExternalData();
        Data data = new Data();
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());
        externalData.setData(data);
        externalData.setSensitiveData(sensitiveData);
        externalData.setAppointmentId("id");
        externalData.setCompanyNumber("companyNumber");
        externalData.setInternalId("internalId");
        externalData.setOfficerId("officerId");
        externalData.setPreviousOfficerId("previousOfficerId");
        InternalData internalData = new InternalData();
        internalData.setOfficerRoleSortOrder(22);
        internalData.setDeltaAt(OffsetDateTime.parse("2022-01-13T00:00:00Z"));
        internalData.setUpdatedBy("updatedBy");
        output.setExternalData(externalData);
        output.setInternalData(internalData);
        return output;
    }

    @NotNull
    private static CompanyAppointmentDocument buildDeltaAppointmentDocument(Instant existingDeltaAt,
            DeltaOfficerData data, DeltaSensitiveData sensitiveData) {
        return new CompanyAppointmentDocument()
                .id("id")
                .data(data)
                .sensitiveData(sensitiveData)
                .internalId("internalId")
                .appointmentId("appointmentId")
                .officerId("officerId")
                .previousOfficerId("previousOfficerId")
                .companyNumber("companyNumber")
                .updated(null)
                .updatedBy("updatedBy")
                .created(null)
                .deltaAt(existingDeltaAt)
                .officerRoleSortOrder(22)
                .companyName("company name")
                .companyStatus("company status");
    }

    private static CompanyAppointmentDocument builtDeltaAppointmentApi(DeltaOfficerData data,
            DeltaSensitiveData sensitiveData, Instant deltaAt) {
        return new CompanyAppointmentDocument()
                .id("id")
                .data(data)
                .sensitiveData(sensitiveData)
                .internalId("internalId")
                .appointmentId("id")
                .officerId("officerId")
                .previousOfficerId("previousOfficerId")
                .companyNumber("companyNumber")
                .updatedBy("updatedBy")
                .deltaAt(deltaAt)
                .officerRoleSortOrder(22);
    }
}
