package uk.gov.companieshouse.company_appointments.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.dao.DataAccessException;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordServiceTest {

    private CompanyAppointmentFullRecordService companyAppointmentService;

    @Mock
    private DeltaAppointmentTransformer deltaAppointmentTransformer;
    @Mock
    private CompanyAppointmentFullRecordRepository companyAppointmentRepository;
    @Mock
    private ResourceChangedApiService resourceChangedApiService;
    @Captor
    private ArgumentCaptor<CompanyAppointmentDocument> captor;

    private final FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = buildFullRecordOfficer();


    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";
    private final static String CONTEXT_ID = "contextId";
    private final static OffsetDateTime DELTA_AT_LATER = OffsetDateTime.parse("2022-01-14T00:00:00.000000Z");
    private final static OffsetDateTime DELTA_AT_STALE = OffsetDateTime.parse("2022-01-12T00:00:00.000000Z");
    private final static Clock CLOCK = Clock.fixed(Instant.parse("2021-08-01T00:00:00.000000Z"),
            ZoneId.of("UTC"));

    private static Stream<Arguments> deltaAtTestCases() {
        return Stream.of(
                // existingDelta, incomingDelta, deltaExists, shouldBeStale
                Arguments.of("2022-01-13T00:00:00.000000Z", DELTA_AT_LATER, false, false),
                // delta does not exist
                Arguments.of("2022-01-13T00:00:00.000000Z", DELTA_AT_LATER, true, false),
                // Newer timestamp not stale
                Arguments.of("2022-01-13T00:00:00.000000Z", DELTA_AT_STALE, true, true),
                // Older timestamp stale
                Arguments.of("2022-01-12T00:00:00.000000Z", DELTA_AT_STALE, true, true)
                // 1 == 1 so delta should be stale
        );
    }

    @BeforeEach
    void setUp() {
        companyAppointmentService =
                new CompanyAppointmentFullRecordService(deltaAppointmentTransformer,
                        companyAppointmentRepository, resourceChangedApiService, CLOCK);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        // given
        DeltaOfficerData data = new DeltaOfficerData();
        data.setOfficerRole("director");
        DeltaItemLinkTypes linkItem = new DeltaItemLinkTypes();
        linkItem.setOfficer(new DeltaOfficerLinkTypes());
        linkItem.setSelf("self");
        data.setLinks(linkItem);
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
        assertThat(result).isInstanceOf(CompanyAppointmentFullRecordView.class);
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
    void testPutAppointmentData() throws Exception {
        // given
        DeltaOfficerData data = new DeltaOfficerData()
                .setOfficerRole("director")
                .setEtag("etag");
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
        companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).insertOrUpdate(captor.capture());
        assertNotNull(captor.getValue().getData().getEtag());
    }

    @Test
    void testPutAppointmentDataThrowsServiceUnavailableException() throws Exception {
        // given
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(new CompanyAppointmentDocument());
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(),
                any())).thenThrow(new DataAccessException("..."){ });

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void testPutAppointmentDataThrowsServiceUnavailableExceptionWhenIllegalArgumentExceptionCaught() throws Exception {
        // given
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class))).thenReturn(new CompanyAppointmentDocument());
        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(IllegalArgumentException.class);

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void testPutAppointmentDataThrowsNotFoundExceptionWhenIllegalArgumentExceptionCaught() throws Exception {
        // given
        doThrow(IllegalArgumentException.class)
                .when(deltaAppointmentTransformer).transform(any(FullRecordCompanyOfficerApi.class));

        // When
        Executable executable = () -> companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        assertThrows(NotFoundException.class, executable);
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testRejectStaleDelta(
            final Instant existingDeltaAt,
            final OffsetDateTime incomingDeltaAt,
            boolean deltaExists,
            boolean shouldBeStale) throws Exception {

        // given
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(incomingDeltaAt);

        DeltaOfficerData data = new DeltaOfficerData();
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
        companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        VerificationMode expectedTimes = (deltaExists && shouldBeStale) ? never() : times(1);
        verify(companyAppointmentRepository, expectedTimes).insertOrUpdate(
                any(CompanyAppointmentDocument.class));
    }

    @Test
    void deleteOfficer() throws Exception {
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER,
                APPOINTMENT_ID))
                .thenReturn(Optional.of(new CompanyAppointmentDocument()));

        companyAppointmentService.deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        verify(companyAppointmentRepository).deleteByCompanyNumberAndID(COMPANY_NUMBER,
                APPOINTMENT_ID);
    }

    @Test
    void testServiceThrows500WhenTransformFails() throws Exception {
        when(deltaAppointmentTransformer.transform(any(FullRecordCompanyOfficerApi.class)))
                .thenThrow(new FailedToTransformException("message"));

        Assert.assertThrows(ServiceUnavailableException.class, () ->
                companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, new FullRecordCompanyOfficerApi()));
    }

    @Test
    void deleteOfficerThrowsNotFound() {
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER,
                APPOINTMENT_ID))
                .thenReturn(Optional.empty());

        Executable executable = () -> companyAppointmentService.deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER,
                APPOINTMENT_ID);

        assertThrows(NotFoundException.class, executable);
    }

    @Test
    void testDeleteAppointmentDataThrowsServiceUnavailableException() {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(any(),
                any())).thenThrow(new DataAccessException("..."){ });

        // When
        Executable executable = () -> companyAppointmentService.deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void testDeleteAppointmentDataThrowsServiceUnavailableExceptionWhenIllegalArgumentExceptionCaught() throws ServiceUnavailableException {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(new CompanyAppointmentDocument()));
        when(resourceChangedApiService.invokeChsKafkaApi(any())).thenThrow(IllegalArgumentException.class);

        // When
        Executable executable = () -> companyAppointmentService.deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
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
        return CompanyAppointmentDocument.Builder.builder()
                .withId("id")
                .withData(data)
                .withSensitiveData(sensitiveData)
                .withInternalId("internalId")
                .withAppointmentId("appointmentId")
                .withOfficerId("officerId")
                .withPreviousOfficerId("previousOfficerId")
                .withCompanyNumber("companyNumber")
                .withUpdated(null)
                .withUpdatedBy("updatedBy")
                .withCreated(null)
                .withDeltaAt(existingDeltaAt)
                .withOfficerRoleSortOrder(22)
                .withCompanyName("company name")
                .withCompanyStatus("company status")
                .build();
    }

    private static CompanyAppointmentDocument builtDeltaAppointmentApi(DeltaOfficerData data,
            DeltaSensitiveData sensitiveData, Instant deltaAt) {
        return new CompanyAppointmentDocument()
                .setId("id")
                .setData(data)
                .setSensitiveData(sensitiveData)
                .setInternalId("internalId")
                .setAppointmentId("id")
                .setOfficerId("officerId")
                .setPreviousOfficerId("previousOfficerId")
                .setCompanyNumber("companyNumber")
                .setUpdatedBy("updatedBy")
                .setDeltaAt(deltaAt)
                .setOfficerRoleSortOrder(22);
    }
}
