package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.CONVERTED_CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.DISSOLVED;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;

class FilterServiceTest {

    private static final String OFFICER_ID = "officerId";

    private FilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new FilterService();
    }

    @DisplayName("Should prepare the service filter successfully")
    @Test
    void prepareFilter() throws BadRequestException {
        // given
        Filter expected = new Filter(true,
                List.of(DISSOLVED.getStatus(), CONVERTED_CLOSED.getStatus(), CLOSED.getStatus()));

        // when
        Filter actual = filterService.prepareFilter("active", OFFICER_ID);

        // then
        assertEquals(expected.isFilterEnabled(), actual.isFilterEnabled());
        assertEquals(expected.filterStatuses(), actual.filterStatuses());
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
        assertEquals(expected.filterStatuses(), actual.filterStatuses());
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
        assertEquals(expected.filterStatuses(), actual.filterStatuses());
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

    @DisplayName("Should return first active appointment in list")
    @Test
    void findFirstActiveAppointment() throws BadRequestException {
        // given
        CompanyAppointmentDocument expected = new CompanyAppointmentDocument()
                .companyStatus("active")
                .data(new DeltaOfficerData());
        List<CompanyAppointmentDocument> documents = List.of(
                expected,
                new CompanyAppointmentDocument()
                        .companyStatus("dissolved")
                        .data(new DeltaOfficerData()),
                new CompanyAppointmentDocument()
                        .companyStatus("active")
                        .data(new DeltaOfficerData()
                                .setResignedOn(Instant.parse("2024-07-15T00:00:00Z"))));
        // when
        Optional<CompanyAppointmentDocument> actual = filterService.findFirstActiveAppointment(documents);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @DisplayName("Should return empty when no active appointment in list")
    @Test
    void findFirstActiveAppointmentEmpty() throws BadRequestException {
        // given
        List<CompanyAppointmentDocument> documents = List.of(
                new CompanyAppointmentDocument()
                        .companyStatus("dissolved")
                        .data(new DeltaOfficerData()),
                new CompanyAppointmentDocument()
                        .companyStatus("active")
                        .data(new DeltaOfficerData()
                                .setResignedOn(Instant.parse("2024-07-15T00:00:00Z"))));
        // when
        Optional<CompanyAppointmentDocument> actual = filterService.findFirstActiveAppointment(documents);

        // then
        assertTrue(actual.isEmpty());
    }
}