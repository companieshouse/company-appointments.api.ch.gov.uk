package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;

@ReadingConverter
public class CompanyAppointmentReadConverter implements Converter<Document, PatchAppointmentNameStatusApi> {

    private final ObjectMapper objectMapper;

    public CompanyAppointmentReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PatchAppointmentNameStatusApi convert(@NonNull Document source) {
        try {
            return objectMapper.readValue(source.toJson(), PatchAppointmentNameStatusApi.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}


