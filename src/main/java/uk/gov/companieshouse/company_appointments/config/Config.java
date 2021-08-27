package uk.gov.companieshouse.company_appointments.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.RequestLoggingInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class Config implements WebMvcConfigurer {
    @Autowired
    private RequestLoggingInterceptor loggingInterceptor;

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private void setNamingStrategy(MongoMappingContext mappingContext) {
        mappingContext.setFieldNamingStrategy(new JsonNamingStrategy());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Logger logger() {
        return LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(authenticationInterceptor);
    }

}
