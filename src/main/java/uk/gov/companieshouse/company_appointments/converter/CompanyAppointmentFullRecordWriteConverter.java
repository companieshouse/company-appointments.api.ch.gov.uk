package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;

@WritingConverter
public class CompanyAppointmentFullRecordWriteConverter implements Converter<FullRecordCompanyOfficerApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public CompanyAppointmentFullRecordWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public BasicDBObject convert(FullRecordCompanyOfficerApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
