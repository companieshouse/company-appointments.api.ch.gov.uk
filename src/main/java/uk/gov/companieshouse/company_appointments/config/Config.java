package uk.gov.companieshouse.company_appointments.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.FullRecordAuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.RequestLoggingInterceptor;

@Configuration
public class Config implements WebMvcConfigurer {
    public static final String PATTERN_FULL_RECORD = "/**/full_record";
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

}
