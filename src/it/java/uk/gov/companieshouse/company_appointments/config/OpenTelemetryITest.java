package uk.gov.companieshouse.company_appointments.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the service's real {@code src/main/resources/application.properties} enables OpenTelemetry
 * when {@code OTEL_LOG_ENABLED=true} and the application context starts with it. The real file is
 * loaded explicitly because {@code src/test/resources/application.properties} shadows it on the test
 * classpath.
 */
@TestPropertySource(
        locations = "file:src/main/resources/application.properties",
        properties = {
                "OTEL_LOG_ENABLED=true",
                "OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318"
        })
class OpenTelemetryITest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeAll
    static void start() {
        mongoDBContainer.start();
        kafkaContainer.start();
    }

    @Test
    void contextStartsWithOpenTelemetryEnabled() {
        assertThat(applicationContext.getBean(OpenTelemetryAppenderInitializer.class)).isNotNull();
        assertThat(applicationContext.getBean(OpenTelemetry.class)).isInstanceOf(OpenTelemetrySdk.class);
    }

}
