package uk.gov.companieshouse.company_appointments.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.FullRecordAuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.interceptor.RequestLoggingInterceptor;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Config.class)
class ConfigTest {
    @Autowired
    private Config testConfig;

    @Mock
    private InterceptorRegistry registry;
    @Mock
    private InterceptorRegistration registration;
    @MockBean
    private RequestLoggingInterceptor requestLoggingInterceptor;
    @MockBean
    private AuthenticationInterceptor authenticationInterceptor;
    @MockBean
    private FullRecordAuthenticationInterceptor fullRecordAuthenticationInterceptor;

    @Test
    void addInterceptors() {
        when(registry.addInterceptor(requestLoggingInterceptor)).thenReturn(registration);
        when(registry.addInterceptor(authenticationInterceptor)).thenReturn(registration);
        when(registry.addInterceptor(fullRecordAuthenticationInterceptor)).thenReturn(registration);

        testConfig.addInterceptors(registry);

        InOrder inOrder = inOrder(registry);

        inOrder.verify(registry).addInterceptor(requestLoggingInterceptor);
        inOrder.verify(registry).addInterceptor(authenticationInterceptor);
        inOrder.verify(registry).addInterceptor(fullRecordAuthenticationInterceptor);
        verify(registration).excludePathPatterns(contains("/full_record"));
        verify(registration).addPathPatterns(contains("/full_record"));
        verifyNoMoreInteractions(registry, registration);
    }
}
