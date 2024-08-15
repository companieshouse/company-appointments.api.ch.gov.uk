package uk.gov.companieshouse.company_appointments.interceptor;

import java.util.Arrays;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;

@Component
public class FullRecordAuthenticationInterceptor implements HandlerInterceptor {
    private final AuthenticationHelper authHelper;
    private final Logger logger;

    public FullRecordAuthenticationInterceptor(AuthenticationHelper authHelper, Logger logger) {
        this.authHelper = authHelper;
        this.logger = logger;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final String identityType = authHelper.getAuthorisedIdentityType(request);
        Map<String, Object> logMap = DataMapHolder.getLogMap();

        if (authHelper.isOauth2IdentityType(identityType)) {
            String companyNumber = request.getRequestURI().split("/")[2];

            if (authHelper.isTokenProtectedAndCompanyAuthorised(request, companyNumber)) {
                return true;
            } else {
                logger.errorRequest(request, "User not authorised. Token has insufficient permissions.", logMap);
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        }

        if (!authHelper.isApiKeyIdentityType(identityType)) {
            logMap.put("identityType", identityType);
            logger.errorRequest(request, "User not authorised. Identity type not correct", logMap);

            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        if (!authHelper.isKeyElevatedPrivilegesAuthorised(request)) {
            logMap.put("privileges", Arrays.asList(authHelper.getApiKeyPrivileges(request)));
            logger.errorRequest(request,
                    "User not authorised. API key does not have sufficient privileges.",
                    logMap);

            response.setStatus(HttpStatus.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
