package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsServiceTest.ServiceTestArgument.Builder;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsServiceTest {

    private static final String APPOINTMENT_ID = "appointmentId";
    private static final String OFFICER_ID = "officerId";
    private static final int START_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE_INTERNAL = 500;
    private static final String REMOVED = "removed";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private static final String DISSOLVED = "dissolved";

    @InjectMocks
    private OfficerAppointmentsService service;
    @Mock
    private OfficerAppointmentsRepository repository;
    @Mock
    private OfficerAppointmentsMapper mapper;
    @Mock
    private FilterService filterService;
    @Mock
    private ItemsPerPageService itemsPerPageService;
    @Mock
    private SortingThresholdService sortingThresholdService;
    @Mock
    private AppointmentList appointmentList;
    @Mock
    private CompanyAppointmentDocument companyAppointmentDocument;
    @Mock
    private OfficerAppointments officerAppointments;

    private static Stream<Arguments> getOfficerAppointments() {
        return Stream.of(
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api",
                                new Builder()
                                        .request(
                                                new OfficerAppointmentsRequest(OFFICER_ID, null, null, 35))
                                        .filterEnabled(false)
                                        .startIndex(START_INDEX)
                                        .itemsPerPage(ITEMS_PER_PAGE)
                                        .resignedCount(1)
                                        .inactiveCount(1)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api when filter is empty",
                                new Builder()
                                        .request(new OfficerAppointmentsRequest(OFFICER_ID, "", null, 35))
                                        .filterEnabled(false)
                                        .startIndex(START_INDEX)
                                        .itemsPerPage(ITEMS_PER_PAGE)
                                        .resignedCount(1)
                                        .inactiveCount(1)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments successfully handles paging values over 35",
                                new Builder()
                                        .request(new OfficerAppointmentsRequest(OFFICER_ID, null, 1, 36))
                                        .filterEnabled(false)
                                        .startIndex(1)
                                        .itemsPerPage(36)
                                        .resignedCount(1)
                                        .inactiveCount(1)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments successfully handles paging values of 500",
                                new Builder()
                                        .request(new OfficerAppointmentsRequest(OFFICER_ID, null, 1, 500))
                                        .filterEnabled(false)
                                        .startIndex(1)
                                        .itemsPerPage(MAX_ITEMS_PER_PAGE_INTERNAL)
                                        .resignedCount(1)
                                        .inactiveCount(1)
                                        .build())));
    }

    private static Stream<Arguments> getActiveOfficerAppointments() {
        return Stream.of(
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api when filter is active",
                                new Builder()
                                        .request(
                                                new OfficerAppointmentsRequest(OFFICER_ID, "active", null, 35))
                                        .filterEnabled(true)
                                        .filterStatuses(List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED))
                                        .startIndex(START_INDEX)
                                        .itemsPerPage(ITEMS_PER_PAGE)
                                        .resignedCount(0)
                                        .inactiveCount(0)
                                        .build())),
                Arguments.of(
                        Named.of(
                                "Get officer appointments returns a paged officer appointments api when paging is provided and filter is active",
                                new Builder()
                                        .request(new OfficerAppointmentsRequest(OFFICER_ID, "active", 3, 3))
                                        .filterEnabled(true)
                                        .filterStatuses(List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED))
                                        .startIndex(3)
                                        .itemsPerPage(3)
                                        .resignedCount(0)
                                        .inactiveCount(0)
                                        .build())));
    }

    @ParameterizedTest
    @MethodSource("getOfficerAppointments")
    void getOfficerAppointments(ServiceTestArgument argument) throws BadRequestException {
        // given
        Filter filter = new Filter(argument.filterEnabled(), argument.filterStatuses());

        when(itemsPerPageService.adjustItemsPerPage(any(), any())).thenReturn(argument.itemsPerPage());
        when(filterService.prepareFilter(any(), any())).thenReturn(filter);
        when(repository.countTotal(any(), anyBoolean(), any())).thenReturn(3);
        when(sortingThresholdService.shouldSort(anyInt(), any())).thenReturn(true);
        when(repository.findOfficerAppointmentsIds(anyString(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(officerAppointments);
        when(officerAppointments.getIds()).thenReturn(List.of(APPOINTMENT_ID));
        when(repository.findFullOfficerAppointments(any())).thenReturn(List.of(companyAppointmentDocument));
        when(repository.countResigned(any())).thenReturn(1);
        when(repository.countInactive(any())).thenReturn(1);
        when(filterService.findFirstActiveAppointment(any())).thenReturn(Optional.of(companyAppointmentDocument));
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(appointmentList));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(argument.request());

        // then
        assertTrue(actual.isPresent());
        assertEquals(appointmentList, actual.get());
        verify(itemsPerPageService).adjustItemsPerPage(argument.itemsPerPage(), null);
        verify(filterService).prepareFilter(argument.request().filter(), OFFICER_ID);
        verify(repository).countTotal(OFFICER_ID, argument.filterEnabled(), argument.filterStatuses());
        verify(sortingThresholdService).shouldSort(3, null);
        verify(repository).findOfficerAppointmentsIds(OFFICER_ID, argument.filterEnabled(), argument.filterStatuses(),
                argument.startIndex(), argument.itemsPerPage());
        verify(repository).findFullOfficerAppointments(List.of(APPOINTMENT_ID));
        verify(repository).countResigned(OFFICER_ID);
        verify(repository).countInactive(OFFICER_ID);
        verify(filterService).findFirstActiveAppointment(List.of(companyAppointmentDocument));
        verify(mapper).mapOfficerAppointments(MapperRequest.builder()
                .startIndex(argument.startIndex())
                .itemsPerPage(argument.itemsPerPage())
                .firstAppointment(companyAppointmentDocument)
                .officerAppointments(List.of(companyAppointmentDocument))
                .totalResults(3)
                .resignedCount(argument.resignedCount())
                .inactiveCount(argument.inactiveCount())
                .build());
    }

    @ParameterizedTest
    @MethodSource("getActiveOfficerAppointments")
    void getActiveOfficerAppointments(ServiceTestArgument argument) throws BadRequestException {
        // given
        Filter filter = new Filter(argument.filterEnabled(), argument.filterStatuses());

        when(itemsPerPageService.adjustItemsPerPage(any(), any())).thenReturn(argument.itemsPerPage());
        when(filterService.prepareFilter(any(), any())).thenReturn(filter);
        when(repository.countTotal(any(), anyBoolean(), any())).thenReturn(3);
        when(sortingThresholdService.shouldSort(anyInt(), any())).thenReturn(true);
        when(repository.findOfficerAppointmentsIds(anyString(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(officerAppointments);
        when(officerAppointments.getIds()).thenReturn(List.of(APPOINTMENT_ID));
        when(repository.findFullOfficerAppointments(any())).thenReturn(List.of(companyAppointmentDocument));
        when(filterService.findFirstActiveAppointment(any())).thenReturn(Optional.of(companyAppointmentDocument));
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(appointmentList));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(argument.request());

        // then
        assertTrue(actual.isPresent());
        assertEquals(appointmentList, actual.get());
        verify(itemsPerPageService).adjustItemsPerPage(argument.itemsPerPage(), null);
        verify(filterService).prepareFilter(argument.request().filter(), OFFICER_ID);
        verify(repository).countTotal(OFFICER_ID, argument.filterEnabled(), argument.filterStatuses());
        verify(sortingThresholdService).shouldSort(3, null);
        verify(repository).findOfficerAppointmentsIds(OFFICER_ID, argument.filterEnabled(), argument.filterStatuses(),
                argument.startIndex(), argument.itemsPerPage());
        verify(repository).findFullOfficerAppointments(List.of(APPOINTMENT_ID));
        verifyNoMoreInteractions(repository);
        verify(filterService).findFirstActiveAppointment(List.of(companyAppointmentDocument));
        verify(mapper).mapOfficerAppointments(MapperRequest.builder()
                .startIndex(argument.startIndex())
                .itemsPerPage(argument.itemsPerPage())
                .firstAppointment(companyAppointmentDocument)
                .officerAppointments(List.of(companyAppointmentDocument))
                .totalResults(3)
                .resignedCount(argument.resignedCount())
                .inactiveCount(argument.inactiveCount())
                .build());
    }

    @DisplayName("Should return call repository findLatestAppointment if no active appointments for officer id")
    @Test
    void getOfficerAppointmentsNonActiveFirstAppointment() throws BadRequestException {
        // given
        OfficerAppointmentsRequest request = OfficerAppointmentsRequest.builder()
                .officerId(OFFICER_ID)
                .itemsPerPage(ITEMS_PER_PAGE)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(any(), any())).thenReturn(ITEMS_PER_PAGE);
        when(filterService.prepareFilter(any(), any())).thenReturn(new Filter(false, List.of()));
        when(repository.countTotal(any(), anyBoolean(), any())).thenReturn(3);
        when(sortingThresholdService.shouldSort(anyInt(), any())).thenReturn(true);
        when(repository.findOfficerAppointmentsIds(anyString(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(officerAppointments);
        when(officerAppointments.getIds()).thenReturn(List.of(APPOINTMENT_ID));
        when(repository.findFullOfficerAppointments(any())).thenReturn(List.of(companyAppointmentDocument));
        when(repository.countResigned(any())).thenReturn(1);
        when(repository.countInactive(any())).thenReturn(1);
        when(filterService.findFirstActiveAppointment(any())).thenReturn(Optional.empty());
        when(repository.findLatestAppointment(any())).thenReturn(companyAppointmentDocument);
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(appointmentList));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(request);

        // then
        assertTrue(actual.isPresent());
        assertEquals(appointmentList, actual.get());
        verify(itemsPerPageService).adjustItemsPerPage(ITEMS_PER_PAGE, null);
        verify(filterService).prepareFilter(null, OFFICER_ID);
        verify(repository).countTotal(OFFICER_ID, false, List.of());
        verify(sortingThresholdService).shouldSort(3, null);
        verify(repository).findOfficerAppointmentsIds(OFFICER_ID, false, List.of(),
                0, ITEMS_PER_PAGE);
        verify(repository).findFullOfficerAppointments(List.of(APPOINTMENT_ID));
        verify(repository).countResigned(OFFICER_ID);
        verify(repository).countInactive(OFFICER_ID);
        verify(filterService).findFirstActiveAppointment(List.of(companyAppointmentDocument));
        verify(repository).findLatestAppointment(OFFICER_ID);
        verify(mapper).mapOfficerAppointments(MapperRequest.builder()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(companyAppointmentDocument)
                .officerAppointments(List.of(companyAppointmentDocument))
                .totalResults(3)
                .resignedCount(1)
                .inactiveCount(1)
                .build());
    }

    @DisplayName("Should return empty optional when no appointments found for officer id")
    @Test
    void getOfficerAppointmentsEmpty() throws BadRequestException {
        // given
        OfficerAppointmentsRequest request = OfficerAppointmentsRequest.builder()
                .officerId(OFFICER_ID)
                .itemsPerPage(ITEMS_PER_PAGE)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(any(), any())).thenReturn(ITEMS_PER_PAGE);
        when(filterService.prepareFilter(any(), any())).thenReturn(new Filter(false, List.of()));
        when(repository.countTotal(any(), anyBoolean(), any())).thenReturn(0);
        when(sortingThresholdService.shouldSort(anyInt(), any())).thenReturn(true);
        when(repository.findOfficerAppointmentsIds(any(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(officerAppointments);
        when(officerAppointments.getIds()).thenReturn(List.of());

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(request);

        // then
        assertTrue(actual.isEmpty());
        verify(itemsPerPageService).adjustItemsPerPage(ITEMS_PER_PAGE, null);
        verify(filterService).prepareFilter(null, OFFICER_ID);
        verify(repository).countTotal(OFFICER_ID, false, List.of());
        verify(sortingThresholdService).shouldSort(0, null);
        verify(repository).findOfficerAppointmentsIds(OFFICER_ID, false, List.of(), 0, ITEMS_PER_PAGE);
        verify(repository).countResigned(OFFICER_ID);
        verify(repository).countInactive(OFFICER_ID);
        verify(filterService).findFirstActiveAppointment(List.of());
        verify(repository).findLatestAppointment(OFFICER_ID);
        verifyNoMoreInteractions(repository);
        verify(mapper).mapOfficerAppointments(MapperRequest.builder()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .officerAppointments(List.of())
                .totalResults(0)
                .resignedCount(0)
                .inactiveCount(0)
                .build());
    }

    @Test
    void getOfficerAppointmentsUnsortedOverThreshold() throws BadRequestException {
        // given
        String authPrivileges = "internal";
        OfficerAppointmentsRequest request = OfficerAppointmentsRequest.builder()
                .officerId(OFFICER_ID)
                .itemsPerPage(ITEMS_PER_PAGE)
                .authPrivileges(authPrivileges)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(any(), any())).thenReturn(ITEMS_PER_PAGE);
        when(filterService.prepareFilter(any(), any())).thenReturn(new Filter(false, List.of()));
        when(repository.countTotal(any(), anyBoolean(), any())).thenReturn(501);
        when(sortingThresholdService.shouldSort(anyInt(), any())).thenReturn(false);
        when(repository.findOfficerAppointments(anyString(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(companyAppointmentDocument));
        when(repository.countResigned(any())).thenReturn(1);
        when(repository.countInactive(any())).thenReturn(1);
        when(filterService.findFirstActiveAppointment(any())).thenReturn(Optional.of(companyAppointmentDocument));
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(appointmentList));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(request);

        // then
        assertTrue(actual.isPresent());
        assertEquals(appointmentList, actual.get());
        verify(itemsPerPageService).adjustItemsPerPage(ITEMS_PER_PAGE, authPrivileges);
        verify(filterService).prepareFilter(null, OFFICER_ID);
        verify(repository).countTotal(OFFICER_ID, false, List.of());
        verify(sortingThresholdService).shouldSort(501, authPrivileges);
        verify(repository).findOfficerAppointments(OFFICER_ID, false, List.of(),
                0, ITEMS_PER_PAGE);
        verify(repository).countResigned(OFFICER_ID);
        verify(repository).countInactive(OFFICER_ID);
        verify(filterService).findFirstActiveAppointment(List.of(companyAppointmentDocument));
        verifyNoMoreInteractions(repository);
        verify(mapper).mapOfficerAppointments(MapperRequest.builder()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(companyAppointmentDocument)
                .officerAppointments(List.of(companyAppointmentDocument))
                .totalResults(501)
                .resignedCount(1)
                .inactiveCount(1)
                .build());
    }

    @Test
    void getOfficerAppointmentsSortedUnderThreshold() throws BadRequestException {
        // given
        OfficerAppointmentsRequest request = OfficerAppointmentsRequest.builder()
                .officerId(OFFICER_ID)
                .itemsPerPage(ITEMS_PER_PAGE)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(any(), any())).thenReturn(ITEMS_PER_PAGE);
        when(filterService.prepareFilter(any(), any())).thenReturn(new Filter(false, List.of()));
        when(repository.countTotal(any(), anyBoolean(), any())).thenReturn(499);
        when(sortingThresholdService.shouldSort(anyInt(), any())).thenReturn(true);
        when(repository.findOfficerAppointmentsIds(anyString(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(officerAppointments);
        when(officerAppointments.getIds()).thenReturn(List.of(APPOINTMENT_ID));
        when(repository.findFullOfficerAppointments(any())).thenReturn(List.of(companyAppointmentDocument));
        when(repository.countResigned(any())).thenReturn(1);
        when(repository.countInactive(any())).thenReturn(1);
        when(filterService.findFirstActiveAppointment(any())).thenReturn(Optional.of(companyAppointmentDocument));
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(appointmentList));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(request);

        // then
        assertTrue(actual.isPresent());
        assertEquals(appointmentList, actual.get());
        verify(itemsPerPageService).adjustItemsPerPage(ITEMS_PER_PAGE, null);
        verify(filterService).prepareFilter(null, OFFICER_ID);
        verify(repository).countTotal(OFFICER_ID, false, List.of());
        verify(sortingThresholdService).shouldSort(499, null);
        verify(repository).findOfficerAppointmentsIds(OFFICER_ID, false, List.of(), 0, ITEMS_PER_PAGE);
        verify(repository).findFullOfficerAppointments(List.of(APPOINTMENT_ID));
        verify(repository).countResigned(OFFICER_ID);
        verify(repository).countInactive(OFFICER_ID);
        verify(filterService).findFirstActiveAppointment(List.of(companyAppointmentDocument));
        verifyNoMoreInteractions(repository);
        verify(mapper).mapOfficerAppointments(MapperRequest.builder()
                .startIndex(START_INDEX)
                .itemsPerPage(ITEMS_PER_PAGE)
                .firstAppointment(companyAppointmentDocument)
                .officerAppointments(List.of(companyAppointmentDocument))
                .totalResults(499)
                .resignedCount(1)
                .inactiveCount(1)
                .build());
    }

    record ServiceTestArgument(OfficerAppointmentsRequest request, boolean filterEnabled, List<String> filterStatuses,
                               int startIndex, int itemsPerPage, int resignedCount, int inactiveCount) {

        private ServiceTestArgument(Builder builder) {
            this(builder.request, builder.filterEnabled, builder.filterStatuses, builder.startIndex,
                    builder.itemsPerPage, builder.resignedCount, builder.inactiveCount);
        }

        static final class Builder {

            private OfficerAppointmentsRequest request;
            private boolean filterEnabled;
            private List<String> filterStatuses;
            private int startIndex;
            private int itemsPerPage;
            private int resignedCount;
            private int inactiveCount;

            private Builder() {
            }

            Builder request(OfficerAppointmentsRequest request) {
                this.request = request;
                return this;
            }

            Builder filterEnabled(boolean filterEnabled) {
                this.filterEnabled = filterEnabled;
                return this;
            }

            Builder filterStatuses(List<String> filterStatuses) {
                this.filterStatuses = filterStatuses;
                return this;
            }

            Builder startIndex(int startIndex) {
                this.startIndex = startIndex;
                return this;
            }

            Builder itemsPerPage(int itemsPerPage) {
                this.itemsPerPage = itemsPerPage;
                return this;
            }

            public Builder resignedCount(int resignedCount) {
                this.resignedCount = resignedCount;
                return this;
            }

            public Builder inactiveCount(int inactiveCount) {
                this.inactiveCount = inactiveCount;
                return this;
            }

            ServiceTestArgument build() {
                return new ServiceTestArgument(this);
            }
        }
    }
}