package uk.gov.companieshouse.company_appointments.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;

@ReadingConverter
public class CompanyAppointmentFullRecordReadConverter implements Converter<Document, FullRecordCompanyOfficerApi> {

    private final ObjectMapper objectMapper;

    public CompanyAppointmentFullRecordReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public FullRecordCompanyOfficerApi convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), FullRecordCompanyOfficerApi.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
