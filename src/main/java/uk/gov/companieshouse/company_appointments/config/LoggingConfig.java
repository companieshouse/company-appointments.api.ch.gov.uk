package uk.gov.companieshouse.company_appointments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.interceptor.RequestLoggingInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class LoggingConfig {

    public LoggingConfig() {
        // blank no-arg constructor
    }

    /**
     * Creates a logger with specified namespace.
     *
     * @return logger
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);
    }

    /**
     * Creates a loggingInterceptor with specified logger.
     *
     * @param logger sets a structured logging logger
     * @return new {@code LoggingInterceptor}
     */
    @Bean("loggingInterceptorBean")
    public RequestLoggingInterceptor loggingInterceptor(Logger logger) {
        return new RequestLoggingInterceptor(logger);
    }

}
