package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;

@ReadingConverter
public class CompanyAppointmentReadConverter implements Converter<Document, PatchAppointmentNameStatusApi> {

    private final ObjectMapper objectMapper;

    public CompanyAppointmentReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public PatchAppointmentNameStatusApi convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), PatchAppointmentNameStatusApi.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}


