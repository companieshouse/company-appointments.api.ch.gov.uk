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

import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class AuthenticationInterceptorTest {

    private AuthenticationInterceptor authenticationInterceptor;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @BeforeEach
    void setUp() {
        authenticationInterceptor = new AuthenticationInterceptor(logger);
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
        when(request.getHeader("ERIC-Identity")).thenReturn("");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsNull() {
        // given
        when(request.getHeader("ERIC-Identity")).thenReturn("user");
        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsEmpty() {
        // given
        when(request.getHeader("ERIC-Identity")).thenReturn("user");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsInvalid() {
        // given
        when(request.getHeader("ERIC-Identity")).thenReturn("user");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("stream");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeKey() {
        // given
        when(request.getHeader("ERIC-Identity")).thenReturn("user");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("key");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeOAuth() {
        // given
        when(request.getHeader("ERIC-Identity")).thenReturn("user");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("oauth");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }
}
