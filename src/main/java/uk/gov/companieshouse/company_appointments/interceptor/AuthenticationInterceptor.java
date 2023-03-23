package uk.gov.companieshouse.company_appointments.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private final Logger logger;

    private final AuthenticationHelper authenticationHelper;

    public AuthenticationInterceptor(Logger logger, AuthenticationHelper authenticationHelper) {
        this.logger = logger;
        this.authenticationHelper = authenticationHelper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String identityType = request.getHeader(ERIC_IDENTITY_TYPE);

        if (StringUtils.isEmpty(request.getHeader(ERIC_IDENTITY)) ||
                (StringUtils.isEmpty(identityType) || isInvalidIdentityType(identityType))) {
            logger.infoRequest(request, "User not authenticated", new HashMap<>());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (!isKeyAuthorised(request, identityType)) {
            logger.info("Supplied key does not have sufficient privilege for the action");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        logger.debugRequest(request, "User authenticated", new HashMap<>());
        return true;
    }

    private boolean isKeyAuthorised(HttpServletRequest request, String ericIdentityType) {
        String[] privileges = authenticationHelper.getApiKeyPrivileges(request);

        return HttpMethod.GET.matches(request.getMethod())
                || ("Key".equalsIgnoreCase(ericIdentityType)
                && ArrayUtils.contains(privileges, "internal-app"));
    }

    private boolean isInvalidIdentityType(String identityType) {
        return !("key".equalsIgnoreCase(identityType) || "oauth2".equalsIgnoreCase(identityType));
    }
}
