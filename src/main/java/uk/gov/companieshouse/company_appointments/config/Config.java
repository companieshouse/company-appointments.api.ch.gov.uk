package uk.gov.companieshouse.company_appointments.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentFullRecordReadConverter;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentFullRecordWriteConverter;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentReadConverter;
import uk.gov.companieshouse.company_appointments.converter.CompanyAppointmentWriteConverter;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.FullRecordAuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.RequestLoggingInterceptor;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateSerializer;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateTimeDeSerializer;
import uk.gov.companieshouse.company_appointments.serialization.LocalDateTimeSerializer;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Supplier;

@Configuration
public class Config implements WebMvcConfigurer {
    public static final String PATTERN_FULL_RECORD = "/**/full_record/**";
    @Autowired
    private RequestLoggingInterceptor loggingInterceptor;

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(authenticationInterceptor).excludePathPatterns(PATTERN_FULL_RECORD);
        registry.addInterceptor(fullRecordAuthenticationInterceptor).addPathPatterns(PATTERN_FULL_RECORD);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Supplier<String> offsetDateTimeGenerator() {
        return () -> String.valueOf(OffsetDateTime.now());
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(
                new CompanyAppointmentReadConverter(objectMapper),
                new CompanyAppointmentWriteConverter(objectMapper),
                new CompanyAppointmentFullRecordWriteConverter(objectMapper),
                new CompanyAppointmentFullRecordReadConverter(objectMapper)));
    }

    private ObjectMapper mongoDbObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeSerializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
