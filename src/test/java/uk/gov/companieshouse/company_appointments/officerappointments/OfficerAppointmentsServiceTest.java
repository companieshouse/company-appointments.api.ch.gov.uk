package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.api.officer.AppointmentList;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentsServiceTest {

    private static final String OFFICER_ID = "officerId";
    private static final int START_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 35;

    @InjectMocks
    private OfficerAppointmentsService service;

    @Mock
    private OfficerAppointmentsRepository repository;

    @Mock
    private OfficerAppointmentsMapper mapper;

    @Mock
    private AppointmentList officerAppointments;

    @Mock
    private OfficerAppointmentsAggregate officerAppointmentsAggregate;

    @Mock
    private AppointmentApi appointmentApi;

    private static Stream<Arguments> serviceTestParameters() {
        return Stream.of(
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api",
                                ServiceTestArgument.ServiceTestArgumentBuilder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, "", null, null))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilter(false)
                                        .withStartIndex(START_INDEX)
                                        .withItemsPerPage(ITEMS_PER_PAGE)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api when filter is null",
                                ServiceTestArgument.ServiceTestArgumentBuilder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, null, null, null))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilter(false)
                                        .withStartIndex(START_INDEX)
                                        .withItemsPerPage(ITEMS_PER_PAGE)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments returns an officer appointments api when filter is active",
                                ServiceTestArgument.ServiceTestArgumentBuilder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, "active", null, null))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilter(true)
                                        .withStartIndex(START_INDEX)
                                        .withItemsPerPage(ITEMS_PER_PAGE)
                                        .build())),
                Arguments.of(
                        Named.of("Get officer appointments returns a paged officer appointments api when paging is provided",
                                ServiceTestArgument.ServiceTestArgumentBuilder()
                                        .withRequest(new OfficerAppointmentsRequest(OFFICER_ID, "active", 3, 3))
                                        .withOfficerId(OFFICER_ID)
                                        .withFilter(true)
                                        .withStartIndex(3)
                                        .withItemsPerPage(3)
                                        .build())));
    }

    @ParameterizedTest
    @MethodSource("serviceTestParameters")
    void getOfficerAppointments(ServiceTestArgument argument) {
        // given
        when(repository.findOfficerAppointments(anyString(), anyBoolean(), anyInt(), anyInt())).thenReturn(officerAppointmentsAggregate);
        when(mapper.mapOfficerAppointments(anyInt(), anyInt(), any())).thenReturn(Optional.of(officerAppointments));

        // when
        Optional<AppointmentList> actual = service.getOfficerAppointments(argument.getRequest());

        // then
        assertTrue(actual.isPresent());
        assertEquals(officerAppointments, actual.get());
        verify(repository).findOfficerAppointments(argument.getOfficerId(), argument.isFilter(), argument.getStartIndex(), argument.getItemsPerPage());
        verify(mapper).mapOfficerAppointments(argument.getStartIndex(), argument.getItemsPerPage(), officerAppointmentsAggregate);
    }

    private static class ServiceTestArgument {
        private final OfficerAppointmentsRequest request;
        private final String officerId;
        private final boolean filter;
        private final int startIndex;
        private final int itemsPerPage;

        public ServiceTestArgument(OfficerAppointmentsRequest request, String officerId, boolean filter, int startIndex, int itemsPerPage) {
            this.request = request;
            this.officerId = officerId;
            this.filter = filter;
            this.startIndex = startIndex;
            this.itemsPerPage = itemsPerPage;
        }

        public static ServiceTestArgumentBuilder ServiceTestArgumentBuilder() {
            return new ServiceTestArgumentBuilder();
        }

        public OfficerAppointmentsRequest getRequest() {
            return request;
        }

        public String getOfficerId() {
            return officerId;
        }

        public boolean isFilter() {
            return filter;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        private static class ServiceTestArgumentBuilder {
            private OfficerAppointmentsRequest request;
            private String officerId;
            private boolean filter;
            private int startIndex;
            private int itemsPerPage;

            public ServiceTestArgumentBuilder withRequest(OfficerAppointmentsRequest request) {
                this.request = request;
                return this;
            }

            public ServiceTestArgumentBuilder withOfficerId(String officerId) {
                this.officerId = officerId;
                return this;
            }

            public ServiceTestArgumentBuilder withFilter(boolean filter) {
                this.filter = filter;
                return this;
            }

            public ServiceTestArgumentBuilder withStartIndex(int startIndex) {
                this.startIndex = startIndex;
                return this;
            }

            public ServiceTestArgumentBuilder withItemsPerPage(int itemsPerPage) {
                this.itemsPerPage = itemsPerPage;
                return this;
            }

            public ServiceTestArgument build() {
                return new ServiceTestArgument(request, officerId, filter, startIndex, itemsPerPage);
            }
        }
    }
}