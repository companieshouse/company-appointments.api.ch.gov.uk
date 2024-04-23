package uk.gov.companieshouse.company_appointments.config;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.interceptor.RequestLoggingInterceptor;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class LoggingConfigTest {
    private LoggingConfig testConfig;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testConfig = new LoggingConfig();
    }

    @Test
    void constructor() {
        assertThat(testConfig, is(instanceOf(LoggingConfig.class)));
    }

    @Test
    void logger() {
        final Logger logger = testConfig.logger();

        assertThat(ReflectionTestUtils.getField(logger, "namespace"),
                is(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE));
    }

    @Test
    void loggingInterceptor() {
        final RequestLoggingInterceptor interceptor = testConfig.loggingInterceptor(logger);

        assertThat(ReflectionTestUtils.getField(interceptor, "logger"), is(logger));
    }
}
