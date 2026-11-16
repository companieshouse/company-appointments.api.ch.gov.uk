package uk.gov.companieshouse.company_appointments.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTelemetryAppenderInitializerTest {

    @Mock
    private OpenTelemetry openTelemetry;

    @Test
    @DisplayName("afterPropertiesSet installs the Logback appender against the injected OpenTelemetry instance")
    void afterPropertiesSetInstallsAppenderWithInjectedOpenTelemetry() {
        var initializer = new OpenTelemetryAppenderInitializer(openTelemetry);

        try (MockedStatic<OpenTelemetryAppender> appenderMock = mockStatic(OpenTelemetryAppender.class)) {
            assertDoesNotThrow(initializer::afterPropertiesSet);

            appenderMock.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }

}
