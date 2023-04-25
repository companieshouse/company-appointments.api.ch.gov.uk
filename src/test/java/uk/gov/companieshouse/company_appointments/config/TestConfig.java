package uk.gov.companieshouse.company_appointments.config;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentFullRecordReadConverter;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentFullRecordWriteConverter;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentReadConverter;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentWriteConverter;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateSerializer;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateTimeDeSerializer;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@TestConfiguration
public class TestConfig {

    /**
     * Custom mongo Conversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(
                new CompanyAppointmentReadConverter(objectMapper),
                new CompanyAppointmentWriteConverter(objectMapper),
                new CompanyAppointmentFullRecordWriteConverter(objectMapper),
                new CompanyAppointmentFullRecordReadConverter(objectMapper)));
    }

    /**
     * Custom object mapper with custom settings.
     */
    @Bean
    public ObjectMapper mongoDbObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Exclude properties with null values from being serialised
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeSerializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
