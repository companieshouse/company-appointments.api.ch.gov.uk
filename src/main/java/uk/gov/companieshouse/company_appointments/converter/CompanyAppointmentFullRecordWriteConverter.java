package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;

@WritingConverter
public class CompanyAppointmentFullRecordWriteConverter implements Converter<FullRecordCompanyOfficerApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public CompanyAppointmentFullRecordWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public BasicDBObject convert(@NonNull FullRecordCompanyOfficerApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
