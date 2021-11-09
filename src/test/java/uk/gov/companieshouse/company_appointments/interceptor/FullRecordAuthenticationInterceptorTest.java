package uk.gov.companieshouse.company_appointments.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class FullRecordAuthenticationInterceptorTest {
    private FullRecordAuthenticationInterceptor authenticationInterceptor;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Object handler;
    @Mock
    private AuthenticationHelper authHelper;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        authenticationInterceptor = new FullRecordAuthenticationInterceptor(authHelper, logger);
    }

    @Test
    void preHandleReturnsFalseIfIdentityTypeNotApiKey() {
        // given
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(AuthenticationHelperImpl.OAUTH2_IDENTITY_TYPE);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        checkUnauthorised(actual);
    }

    @Test
    void preHandleReturnsFalseIfApiKeyNotElevated() {
        // given
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(AuthenticationHelperImpl.API_KEY_IDENTITY_TYPE);
        when(authHelper.isApiKeyIdentityType(AuthenticationHelperImpl.API_KEY_IDENTITY_TYPE)).thenReturn(true);
        when(authHelper.isKeyElevatedPrivilegesAuthorised(request)).thenReturn(false);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        checkUnauthorised(actual);
    }

    @ParameterizedTest(name = "invalid identityType: {0}")
    @NullSource
    @ValueSource(strings = {"", "legit"})
    void preHandleReturnsFalseIfEricIdentityTypeIsNull(final String identityType) {
        // given
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(identityType);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        checkUnauthorised(actual);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeKey() {
        // given
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(AuthenticationHelperImpl.API_KEY_IDENTITY_TYPE);
        when(authHelper.isApiKeyIdentityType(AuthenticationHelperImpl.API_KEY_IDENTITY_TYPE)).thenReturn(true);
        when(authHelper.isKeyElevatedPrivilegesAuthorised(request)).thenReturn(true);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        checkAuthorised(actual);
    }

    private void checkUnauthorised(final boolean actual) {
        assertThat(actual, is(false));
        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    private void checkAuthorised(final boolean actual) {
        assertThat(actual, is(true));
        verifyNoInteractions(response);
    }
}
