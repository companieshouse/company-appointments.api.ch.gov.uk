package uk.gov.companieshouse.company_appointments.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String INTERNAL_APP = "internal-app";
    public static final String USER = "user";
    public static final String STREAM = "stream";
    public static final String AUTH_TYPE_OAUTH_2 = "oauth2";
    public static final String AUTH_TYPE_KEY = "key";
    private AuthenticationInterceptor authenticationInterceptor;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private Object handler;

    @BeforeEach
    void setUp() {
        authenticationInterceptor = new AuthenticationInterceptor(logger, authenticationHelper);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityIsNull() {
        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityIsEmpty() {
        // given
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(null);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn("");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsNull() {
        // given
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(null);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsEmpty() {
        // given
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn("");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsInvalid() {
        // given
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(STREAM);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeKey() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(AUTH_TYPE_KEY);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeOAuth() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(AUTH_TYPE_OAUTH_2);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsTrueIfMethodNotGetAndHasInternalPrivileges() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.PATCH.name());
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(AUTH_TYPE_KEY);
        when(authenticationHelper.getApiKeyPrivileges(request))
                .thenReturn(new String[] {INTERNAL_APP});

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsFalseIfMethodNotGetAndHasInternalPrivilegesButIdentityTypeIsOAUTH2() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.PATCH.name());
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(AUTH_TYPE_OAUTH_2);
        when(authenticationHelper.getApiKeyPrivileges(request))
                .thenReturn(new String[] {INTERNAL_APP});

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void preHandleReturnsFalseIfMethodNotGetAndNoInternalPrivileges() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.PATCH.name());
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(AUTH_TYPE_KEY);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }
}
