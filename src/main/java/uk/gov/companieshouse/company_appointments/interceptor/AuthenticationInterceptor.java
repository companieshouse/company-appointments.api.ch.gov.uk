package uk.gov.companieshouse.company_appointments.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;

@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private Logger logger;

    @Autowired
    public AuthenticationInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (StringUtils.isEmpty(request.getHeader(ERIC_IDENTITY)) ||
                (StringUtils.isEmpty(request.getHeader(ERIC_IDENTITY_TYPE)) || isInvalidIdentityType(request))) {
            logger.infoRequest(request, "User not authenticated", new HashMap<>());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        logger.debugRequest(request, "User authenticated", new HashMap<>());
        return true;
    }

    private boolean isInvalidIdentityType(HttpServletRequest request) {
        String identityType = request.getHeader(ERIC_IDENTITY_TYPE);
        return !("key".equalsIgnoreCase(identityType) || "oauth2".equalsIgnoreCase(identityType));
    }

}
