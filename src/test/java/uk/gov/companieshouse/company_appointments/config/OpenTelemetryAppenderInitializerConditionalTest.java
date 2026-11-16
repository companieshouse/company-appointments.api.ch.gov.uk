package uk.gov.companieshouse.company_appointments.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class OpenTelemetryAppenderInitializerConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OpenTelemetryTestConfiguration.class, OpenTelemetryAppenderInitializer.class);

    @Test
    @DisplayName("bean is absent when management.opentelemetry.enabled is not set")
    void beanAbsentWhenPropertyUnset() {
        contextRunner.run(this::assertBeanAbsent);
    }

    @Test
    @DisplayName("bean is absent when management.opentelemetry.enabled is false")
    void beanAbsentWhenPropertyFalse() {
        contextRunner.withPropertyValues("management.opentelemetry.enabled=false")
                .run(this::assertBeanAbsent);
    }

    @Test
    @DisplayName("bean is present when management.opentelemetry.enabled is true")
    void beanPresentWhenPropertyTrue() {
        contextRunner.withPropertyValues("management.opentelemetry.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(OpenTelemetryAppenderInitializer.class));
    }

    private void assertBeanAbsent(AssertableApplicationContext context) {
        assertThat(context).doesNotHaveBean(OpenTelemetryAppenderInitializer.class);
    }

    @Configuration
    static class OpenTelemetryTestConfiguration {

        @Bean
        OpenTelemetry openTelemetry() {
            return OpenTelemetry.noop();
        }

    }

}
