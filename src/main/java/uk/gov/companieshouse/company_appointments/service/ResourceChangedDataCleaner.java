package uk.gov.companieshouse.company_appointments.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResourceChangedDataCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);
    // Jackson 2 defaults keep dates as timestamp arrays (e.g. [2011, 11, 3]) in the resource-changed payload
    private static final JsonMapper NULL_CLEANING_OBJECT_MAPPER = JsonMapper.builderWithJackson2Defaults()
            .changeDefaultPropertyInclusion(inclusion -> inclusion
                    .withValueInclusion(Include.NON_NULL)
                    .withContentInclusion(Include.NON_NULL))
            .build();

    private final CompanyAppointmentMapper companyAppointmentMapper;

    public ResourceChangedDataCleaner(CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public Object cleanOutNullValues(CompanyAppointmentDocument document) {
        try {
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(
                    companyAppointmentMapper.map(document));
            return NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);
        } catch (JacksonException e) {
            LOGGER.error("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw e;
        }
    }

    public Object cleanOutNullValues(OfficerSummary officerSummary) {
        try {
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(officerSummary);
            return NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);
        } catch (JacksonException e) {
            LOGGER.error("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw e;
        }
    }
}

