package uk.gov.companieshouse.company_appointments.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

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
    @Captor
    private ArgumentCaptor<String> companyNumber;

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
        when(authHelper.getApiKeyPrivileges(request)).thenReturn(new String[]{});

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertThat(actual, is(false));
        verify(response).setStatus(HttpStatus.SC_FORBIDDEN);
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

    @Test
    void preHandleReturnsTrueIfOAuth2TokenWithCorrectAuthorisation() {
        when(request.getRequestURI()).thenReturn("/company/00006400/appointments/1/full_record");

        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(AuthenticationHelperImpl.OAUTH2_IDENTITY_TYPE);
        when(authHelper.isOauth2IdentityType(AuthenticationHelperImpl.OAUTH2_IDENTITY_TYPE)).thenReturn(true);
        when(authHelper.isTokenProtectedAndCompanyAuthorised(any(), companyNumber.capture())).thenReturn(true);

        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        checkAuthorised(actual);
        assertThat(companyNumber.getValue(), is("00006400"));
    }

    @Test
    void preHandleReturnsFalseIfOAuth2TokenWithIncorrectAuthorisation() {
        when(request.getRequestURI()).thenReturn("/company/00006400/appointments/1/full_record");

        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(AuthenticationHelperImpl.OAUTH2_IDENTITY_TYPE);
        when(authHelper.isOauth2IdentityType(AuthenticationHelperImpl.OAUTH2_IDENTITY_TYPE)).thenReturn(true);
        when(authHelper.isTokenProtectedAndCompanyAuthorised(any(), companyNumber.capture())).thenReturn(false);

        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        checkUnauthorised(actual);
        assertThat(companyNumber.getValue(), is("00006400"));
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
