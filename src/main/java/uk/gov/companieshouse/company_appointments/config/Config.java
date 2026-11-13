package uk.gov.companieshouse.company_appointments.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.FullRecordAuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.util.EmptyFieldDeserializer;

@Configuration
public class Config implements WebMvcConfigurer {

    public static final String PATTERN_FULL_RECORD = "/**/full_record/**";
    public static final String HEALTHCHECK_PATH = "/healthcheck"; // NOSONAR

    private final AuthenticationInterceptor authenticationInterceptor;
    private final FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor;
    private final String apiKey;
    private final String metricsUrl;
    private final String internalApiUrl;

    public Config(AuthenticationInterceptor authenticationInterceptor,
            FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor,
            @Value("${chs.kafka.api.key}") String apiKey, @Value("${company-metrics-api.endpoint}") String metricsUrl,
            @Value("${chs.kafka.api.endpoint}") String internalApiUrl) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.fullRecordAuthenticationInterceptor = fullRecordAuthenticationInterceptor;
        this.apiKey = apiKey;
        this.metricsUrl = metricsUrl;
        this.internalApiUrl = internalApiUrl;
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
    @Primary
    public JsonMapper jsonMapper() {
        // Jackson 2 defaults (e.g. unsorted properties) preserve the API's existing JSON output
        return JsonMapper.builderWithJackson2Defaults()
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                // Jackson 2's setDateFormat() implicitly disabled timestamps, so dates are written as "yyyy-MM-dd"
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(new SimpleModule().addDeserializer(String.class, new EmptyFieldDeserializer()))
                .changeDefaultPropertyInclusion(inclusion -> inclusion
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.NON_NULL))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }

    @Bean
    public Supplier<InternalApiClient> metricsApiClient() {
        return () -> buildInternalApiClient(metricsUrl);
    }

    @Bean
    public Supplier<InternalApiClient> chsKafkaApiClient() {
        return () -> buildInternalApiClient(internalApiUrl);
    }

    private InternalApiClient buildInternalApiClient(final String url) {
        ApiKeyHttpClient apiKeyHttpClient = new ApiKeyHttpClient(apiKey);
        apiKeyHttpClient.setRequestId(DataMapHolder.getRequestId());

        InternalApiClient internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setBasePath(url);

        return internalApiClient;
    }
}
