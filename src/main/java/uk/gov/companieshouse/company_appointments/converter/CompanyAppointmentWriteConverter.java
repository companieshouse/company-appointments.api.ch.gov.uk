package uk.gov.companieshouse.company_appointments.converter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.appointment.PatchAppointmentNameStatusApi;

@WritingConverter
public class CompanyAppointmentWriteConverter implements Converter<PatchAppointmentNameStatusApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public CompanyAppointmentWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public BasicDBObject convert(PatchAppointmentNameStatusApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
