package uk.gov.companieshouse.company_appointments.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.FullRecordAuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.util.EmptyFieldDeserializer;

@Configuration
public class Config implements WebMvcConfigurer {

    public static final String PATTERN_FULL_RECORD = "/**/full_record/**";
    public static final String HEALTHCHECK_PATH = "/healthcheck";

    private final AuthenticationInterceptor authenticationInterceptor;
    private final FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor;

    public Config(AuthenticationInterceptor authenticationInterceptor,
            FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.fullRecordAuthenticationInterceptor = fullRecordAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .excludePathPatterns(PATTERN_FULL_RECORD)
                .excludePathPatterns(HEALTHCHECK_PATH);
        registry.addInterceptor(fullRecordAuthenticationInterceptor)
                .addPathPatterns(PATTERN_FULL_RECORD);
    }

    /**
     * Obtains a clock that returns the current instant using the best available system clock, converting to date and
     * time using the UTC time-zone.
     *
     * @return a clock that uses the best available system clock in the UTC zone, not null
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Supplier<InternalApiClient> internalApiClientSupplier(
            @Value("${api.api-key}") String apiKey,
            @Value("${api.api-url}") String apiUrl) {
        return () -> {
            InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(
                    apiKey));
            internalApiClient.setBasePath(apiUrl);
            return internalApiClient;
        };
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .registerModule(new SimpleModule().addDeserializer(String.class, new EmptyFieldDeserializer()))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
}
