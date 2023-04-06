package uk.gov.companieshouse.company_appointments;

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
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.service.CompanyAppointmentFullRecordService;
import uk.gov.companieshouse.company_appointments.util.DeltaDateValidator;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentFullRecordServiceTest {

    private CompanyAppointmentFullRecordService companyAppointmentService;

    private CompanyAppointmentMapper mapper;

    @Mock
    private CompanyAppointmentFullRecordRepository companyAppointmentRepository;
    @Mock
    private ResourceChangedApiService resourceChangedApiService;
    @Captor
    private ArgumentCaptor<DeltaAppointmentApiEntity> captor;

    @Mock
    private DeltaAppointmentApiEntity deltaAppointmentApiEntity;

    private FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = buildFullRecordOfficer();;

    @Mock
    private DeltaDateValidator deltaDateValidator;

    private final static String COMPANY_NUMBER = "123456";
    private final static String APPOINTMENT_ID = "345678";
    private final static String CONTEXT_ID = "contextId";
    private final static Instant CREATED_AT = Instant.parse("2021-08-01T00:00:00.000Z");
    private final static OffsetDateTime DELTA_AT_LATER = OffsetDateTime.parse("2022-01-14T00:00:00.000Z");
    private final static OffsetDateTime DELTA_AT_STALE = OffsetDateTime.parse("2022-01-12T00:00:00.000Z");
    private final static InstantAPI instantAPI = new InstantAPI(CREATED_AT);
    private final static Clock CLOCK = Clock.fixed(CREATED_AT, ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        companyAppointmentService =
                new CompanyAppointmentFullRecordService(companyAppointmentRepository, resourceChangedApiService, CLOCK);
    }

    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() throws NotFoundException {
        // given
        Data data = new Data();
        data.setOfficerRole(Data.OfficerRoleEnum.DIRECTOR);
        ItemLinkTypes linkItem = new ItemLinkTypes();
        linkItem.setOfficer(new OfficerLinkTypes());
        linkItem.setSelf("self");
        data.setLinks(Collections.singletonList(linkItem));
        deltaAppointmentApiEntity = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", data, new SensitiveData(), "internalId",
                        "appointmentId", "officerId", "previousOfficerId", "companyNumber",
                        instantAPI, "updatedBy", instantAPI, "deltaAt", 22));

        when(companyAppointmentRepository.readByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID)).thenReturn(Optional.of(deltaAppointmentApiEntity));

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
    void testPutAppointmentData() throws ServiceUnavailableException {
        // given
        Data data = new Data();
        data.setOfficerRole(Data.OfficerRoleEnum.DIRECTOR);
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());
        deltaAppointmentApiEntity = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", data, sensitiveData, "internalId", "id", "officerId",
                        "previousOfficerId", "companyNumber", null,"updatedBy", null, "2022-01-12T00:00Z", 22));
        when(companyAppointmentRepository.readByCompanyNumberAndID(deltaAppointmentApiEntity.getCompanyNumber(),
                deltaAppointmentApiEntity.getId())).thenReturn(Optional.of(deltaAppointmentApiEntity));

        // When
        companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        verify(companyAppointmentRepository).insertOrUpdate(captor.capture());
        assertNotNull(captor.getValue().getEtag());
    }

    @ParameterizedTest
    @MethodSource("deltaAtTestCases")
    void testRejectStaleDelta(
            final String existingDeltaAt,
            final OffsetDateTime incomingDeltaAt,
            boolean deltaExists,
            boolean shouldBeStale) throws ServiceUnavailableException {

        // given
        fullRecordCompanyOfficerApi.getInternalData().setDeltaAt(incomingDeltaAt);

        Data data = new Data();
        SensitiveData sensitiveData = new SensitiveData();
        String expectedCompanyNumber = "companyNumber";
        String expectedId = "id";

        DeltaAppointmentApiEntity appointmentEntity = new DeltaAppointmentApiEntity(
                new DeltaAppointmentApi("id", "etag", data, sensitiveData, "internalId", "id", "officerId",
                        "previousOfficerId", "companyNumber", null,"updatedBy", null, existingDeltaAt, 22));

        when(companyAppointmentRepository.readByCompanyNumberAndID(expectedCompanyNumber,
                expectedId)).thenReturn(deltaExists ? Optional.of(appointmentEntity) : Optional.empty());

        // When
        companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, fullRecordCompanyOfficerApi);

        // then
        VerificationMode expectedTimes = (deltaExists && shouldBeStale) ? never() : times(1);
        verify(companyAppointmentRepository, expectedTimes).insertOrUpdate(any(DeltaAppointmentApi.class));
    }

    private static Stream<Arguments> deltaAtTestCases() {
        return Stream.of(
                // existingDelta, incomingDelta, deltaExists, shouldBeStale
                Arguments.of("2022-01-13T00:00Z", DELTA_AT_LATER, false, false), // delta does not exist
                Arguments.of("2022-01-13T00:00Z", DELTA_AT_LATER, true, false), // Newer timestamp not stale
                Arguments.of("2022-01-13T00:00:000Z", DELTA_AT_STALE, true, true), // shorter string is considered less than
                Arguments.of("2022-01-13T00:00Z", DELTA_AT_STALE, true, true), // Older timestamp stale
                Arguments.of("2022-01-12T00:00Z", DELTA_AT_STALE, true, true) // 1 == 1 so delta should be stale
        );
    }


    @Test
    void deleteOfficer() throws Exception {
        when(companyAppointmentRepository.deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.of(deltaAppointmentApiEntity));

        companyAppointmentService.deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        verify(companyAppointmentRepository).deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID);
    }

    @Test
    void testServiceThrows500WhenTransformFails() {
        Assert.assertThrows(ServiceUnavailableException.class, () ->
                companyAppointmentService.upsertAppointmentDelta(CONTEXT_ID, new FullRecordCompanyOfficerApi()));
    }

    @Test
    void deleteOfficerThrowsNotFound() {
        when(companyAppointmentRepository.deleteByCompanyNumberAndID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(Optional.empty());

        Executable result = () -> companyAppointmentService.deleteAppointmentDelta(CONTEXT_ID, COMPANY_NUMBER, APPOINTMENT_ID);

        assertThrows(NotFoundException.class, result);
    }

    private FullRecordCompanyOfficerApi buildFullRecordOfficer() {
        FullRecordCompanyOfficerApi output  = new FullRecordCompanyOfficerApi();

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

}
