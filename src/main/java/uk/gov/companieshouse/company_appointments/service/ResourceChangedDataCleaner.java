package uk.gov.companieshouse.company_appointments.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.UncheckedIOException;
import org.springframework.stereotype.Component;
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
    private static final ObjectMapper NULL_CLEANING_OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(Include.NON_NULL);

    private final CompanyAppointmentMapper companyAppointmentMapper;

    public ResourceChangedDataCleaner(CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public Object cleanOutNullValues(CompanyAppointmentDocument document) {
        try {
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(
                    companyAppointmentMapper.map(document));
            return NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw new UncheckedIOException(e);
        }
    }

    public Object cleanOutNullValues(OfficerSummary officerSummary) {
        try {
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(officerSummary);
            return NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw new UncheckedIOException(e);
        }
    }
}

