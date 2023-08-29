package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsServiceTest {

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
    private AppointmentList officerAppointments;
    @Mock
    private OfficerAppointmentsAggregate officerAppointmentsAggregate;
    @Mock
    private CompanyAppointmentDocument companyAppointmentDocument;

    private static Stream<Arguments> serviceTestParameters() {
        return Stream.of(
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api",
                                new ServiceTestArgument.Builder()
                                        .withRequest(
                                                new OfficerAppointmentsRequest(OFFICER_ID, null, null, 35))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilterEnabled(false)
                                        .withStartIndex(START_INDEX)
                                        .withItemsPerPage(ITEMS_PER_PAGE)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api when filter is empty",
                                new ServiceTestArgument.Builder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, "", null, 35))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilterEnabled(false)
                                        .withStartIndex(START_INDEX)
                                        .withItemsPerPage(ITEMS_PER_PAGE)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api when filter is active",
                                new ServiceTestArgument.Builder()
                                        .withRequest(
                                                new OfficerAppointmentsRequest(OFFICER_ID, "active", null, 35))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilterEnabled(true)
                                        .withFilterStatuses(List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED))
                                        .withStartIndex(START_INDEX)
                                        .withItemsPerPage(ITEMS_PER_PAGE)
                                        .build())),
                Arguments.of(
                        Named.of(
                                "Get officer appointments returns a paged officer appointments api when paging is provided and filter is active",
                                new ServiceTestArgument.Builder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, "active", 3, 3))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilterEnabled(true)
                                        .withFilterStatuses(List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED))
                                        .withStartIndex(3)
                                        .withItemsPerPage(3)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments successfully handles paging values over 35",
                                new ServiceTestArgument.Builder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, null, 1, 36))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilterEnabled(false)
                                        .withStartIndex(1)
                                        .withItemsPerPage(36)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments successfully handles paging values of 500",
                                new ServiceTestArgument.Builder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, null, 1, 500))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilterEnabled(false)
                                        .withStartIndex(1)
                                        .withItemsPerPage(MAX_ITEMS_PER_PAGE_INTERNAL)
                                        .build())));
    }

    @ParameterizedTest
    @MethodSource("serviceTestParameters")
    void getOfficerAppointments(ServiceTestArgument argument) throws BadRequestException {
        // given
        Filter filter = new Filter(argument.isFilterEnabled(), argument.getFilterStatuses());

        when(repository.findFirstByOfficerId(anyString())).thenReturn(Optional.of(
                companyAppointmentDocument));
        when(filterService.prepareFilter(any(), any())).thenReturn(filter);
        when(repository.findOfficerAppointments(anyString(), anyBoolean(), any(), anyInt(), anyInt())).thenReturn(
                officerAppointmentsAggregate);
        when(mapper.mapOfficerAppointments(any())).thenReturn(Optional.of(officerAppointments));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(argument.getRequest());

        // then
        assertTrue(actual.isPresent());
        assertEquals(officerAppointments, actual.get());
        verify(filterService).prepareFilter(argument.getRequest().getFilter(), argument.getOfficerId());
        verify(repository).findOfficerAppointments(argument.getOfficerId(), argument.isFilterEnabled(),
                argument.getFilterStatuses(), argument.getStartIndex(), argument.getItemsPerPage());
        verify(mapper).mapOfficerAppointments(new MapperRequest()
                .startIndex(argument.getStartIndex())
                .itemsPerPage(argument.getItemsPerPage())
                .firstAppointment(companyAppointmentDocument)
                .aggregate(officerAppointmentsAggregate));
    }

    @DisplayName("Should return empty optional when no appointments found for officer id")
    @Test
    void getOfficerAppointmentsEmpty() throws BadRequestException {
        // given
        when(repository.findFirstByOfficerId(anyString())).thenReturn(Optional.empty());

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(
                new OfficerAppointmentsRequest(OFFICER_ID, null, null, null));

        // then
        assertTrue(actual.isEmpty());
    }

    private static class ServiceTestArgument {

        private final OfficerAppointmentsRequest request;
        private final String officerId;
        private final boolean filterEnabled;
        private final List<String> filterStatuses;
        private final int startIndex;
        private final int itemsPerPage;

        private ServiceTestArgument(Builder builder) {
            this.request = builder.request;
            this.officerId = builder.officerId;
            this.filterEnabled = builder.filterEnabled;
            this.filterStatuses = builder.filterStatuses;
            this.startIndex = builder.startIndex;
            this.itemsPerPage = builder.itemsPerPage;
        }

        public OfficerAppointmentsRequest getRequest() {
            return request;
        }

        public String getOfficerId() {
            return officerId;
        }

        public boolean isFilterEnabled() {
            return filterEnabled;
        }

        public List<String> getFilterStatuses() {
            return filterStatuses;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        private static final class Builder {

            private OfficerAppointmentsRequest request;
            private String officerId;
            private boolean filterEnabled;
            private List<String> filterStatuses;
            private int startIndex;
            private int itemsPerPage;

            private Builder() {
                this.filterStatuses = new ArrayList<>();
            }

            public Builder withRequest(OfficerAppointmentsRequest request) {
                this.request = request;
                return this;
            }

            public Builder withOfficerId(String officerId) {
                this.officerId = officerId;
                return this;
            }

            public Builder withFilterEnabled(boolean filterEnabled) {
                this.filterEnabled = filterEnabled;
                return this;
            }

            public Builder withFilterStatuses(List<String> filterStatuses) {
                this.filterStatuses = filterStatuses;
                return this;
            }

            public Builder withStartIndex(int startIndex) {
                this.startIndex = startIndex;
                return this;
            }

            public Builder withItemsPerPage(int itemsPerPage) {
                this.itemsPerPage = itemsPerPage;
                return this;
            }

            public ServiceTestArgument build() {
                return new ServiceTestArgument(this);
            }
        }
    }
}