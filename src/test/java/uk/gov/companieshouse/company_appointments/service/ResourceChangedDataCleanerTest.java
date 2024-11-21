package uk.gov.companieshouse.company_appointments.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@ExtendWith(MockitoExtension.class)
class ResourceChangedDataCleanerTest {

    @InjectMocks
    private ResourceChangedDataCleaner resourceChangedDataCleaner;

    @Mock
    CompanyAppointmentMapper companyAppointmentMapper;

    @Mock
    private CompanyAppointmentDocument companyAppointmentDocument;

    @Test
    void shouldCleanOutNullValuesFromCompanyAppointmentDocument() {
        // given
        OfficerSummary summary = new OfficerSummary()
                .name("JOHN TESTER")
                .links(new ItemLinkTypes());

        when(companyAppointmentMapper.map(any())).thenReturn(summary);

        // when
        Object actual = resourceChangedDataCleaner.cleanOutNullValues(companyAppointmentDocument);
        final String actualAsString = actual.toString();

        // then
        verify(companyAppointmentMapper).map(companyAppointmentDocument);
        assertFalse(actualAsString.contains("null"));
        assertFalse(actualAsString.contains("appointed_on={}"));
        assertTrue(actualAsString.contains("name=JOHN TESTER"));
        assertTrue(actualAsString.contains("links={}"));
    }

    @Test
    void shouldCleanOutNullValuesFromOfficerSummary() {
        // given
        OfficerSummary summary = new OfficerSummary()
                .name("JOHN TESTER")
                .links(new ItemLinkTypes());

        // when
        Object actual = resourceChangedDataCleaner.cleanOutNullValues(summary);
        final String actualAsString = actual.toString();

        // then
        assertFalse(actualAsString.contains("null"));
        assertFalse(actualAsString.contains("appointed_on={}"));
        assertTrue(actualAsString.contains("name=JOHN TESTER"));
        assertTrue(actualAsString.contains("links={}"));
    }
}