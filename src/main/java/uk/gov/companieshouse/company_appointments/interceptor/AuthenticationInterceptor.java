package uk.gov.companieshouse.company_appointments.interceptor;

import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";

    private final AuthenticationHelper authenticationHelper;

    public AuthenticationInterceptor(AuthenticationHelper authenticationHelper) {
        this.authenticationHelper = authenticationHelper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String identityType = request.getHeader(ERIC_IDENTITY_TYPE);

        if (StringUtils.isEmpty(request.getHeader(ERIC_IDENTITY)) ||
                (StringUtils.isEmpty(identityType) || isInvalidIdentityType(identityType))) {
            LOGGER.errorRequest(request, "User not authenticated", DataMapHolder.getLogMap());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (!isKeyAuthorised(request, identityType)) {
            LOGGER.errorRequest(request, "Supplied key does not have sufficient privilege for the action",
                    DataMapHolder.getLogMap());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        LOGGER.debugRequest(request, "User authenticated", DataMapHolder.getLogMap());
        return true;
    }

    private boolean isKeyAuthorised(HttpServletRequest request, String ericIdentityType) {
        String[] privileges = authenticationHelper.getApiKeyPrivileges(request);

        return HttpMethod.GET.matches(request.getMethod())
                || ("key".equalsIgnoreCase(ericIdentityType)
                && ArrayUtils.contains(privileges, "internal-app"));
    }

    private boolean isInvalidIdentityType(String identityType) {
        return !("key".equalsIgnoreCase(identityType) || "oauth2".equalsIgnoreCase(identityType));
    }
}
