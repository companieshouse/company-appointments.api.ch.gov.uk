package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;

class FilterServiceTest {

    private static final String OFFICER_ID = "officerId";
    private static final String REMOVED = "removed";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private static final String DISSOLVED = "dissolved";

    private FilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new FilterService();
    }

    @DisplayName("Should prepare the service filter successfully")
    @Test
    void prepareFilter() throws BadRequestException {
        // given
        Filter expected = new Filter(true, List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED));

        // when
        Filter actual = filterService.prepareFilter("active", OFFICER_ID);

        // then
        assertEquals(expected.isFilterEnabled(), actual.isFilterEnabled());
        assertEquals(expected.getFilterStatuses(), actual.getFilterStatuses());
    }

    @DisplayName("Should prepare the service filter successfully when filter is empty")
    @Test
    void prepareFilterEmpty() throws BadRequestException {
        // given
        Filter expected = new Filter(false, emptyList());

        // when
        Filter actual = filterService.prepareFilter("", OFFICER_ID);

        // then
        assertEquals(expected.isFilterEnabled(), actual.isFilterEnabled());
        assertEquals(expected.getFilterStatuses(), actual.getFilterStatuses());
    }

    @DisplayName("Should prepare the service filter successfully when filter is null")
    @Test
    void prepareFilterNull() throws BadRequestException {
        // given
        Filter expected = new Filter(false, emptyList());

        // when
        Filter actual = filterService.prepareFilter(null, OFFICER_ID);

        // then
        assertEquals(expected.isFilterEnabled(), actual.isFilterEnabled());
        assertEquals(expected.getFilterStatuses(), actual.getFilterStatuses());
    }

    @DisplayName("Should throw bad request exception when invalid filter parameter supplied")
    @Test
    void prepareFilterInvalid() {
        // given
        // when
        Executable executable = () -> filterService.prepareFilter("invalid", OFFICER_ID);

        // then
        Exception exception = assertThrows(BadRequestException.class, executable);
        assertEquals(String.format("Invalid filter parameter supplied: %s, officer ID: %s", "invalid", OFFICER_ID),
                exception.getMessage());
    }

    @DisplayName("Should throw bad request exception when filter parameter supplied with incorrect casing")
    @Test
    void prepareFilterBadRequest() {
        // given
        // when
        Executable executable = () -> filterService.prepareFilter("Active", OFFICER_ID);

        // then
        Exception exception = assertThrows(BadRequestException.class, executable);
        assertEquals(String.format("Invalid filter parameter supplied: %s, officer ID: %s", "Active", OFFICER_ID),
                exception.getMessage());
    }
}