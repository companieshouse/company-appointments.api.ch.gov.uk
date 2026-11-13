package uk.gov.companieshouse.company_appointments.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Wires the Logback OpenTelemetry appender to the OpenTelemetry instance managed by Spring
 * Boot's native OpenTelemetry starter. Spring Boot 4's starter does not perform this wiring
 * automatically, so it must be done explicitly.
 */
@Component
@ConditionalOnProperty(
        prefix = "management.opentelemetry",
        name = "enabled",
        havingValue = "true")
class OpenTelemetryAppenderInitializer implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    OpenTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        OpenTelemetryAppender.install(this.openTelemetry);
    }

}
