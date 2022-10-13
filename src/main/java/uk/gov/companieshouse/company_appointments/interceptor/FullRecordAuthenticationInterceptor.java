package uk.gov.companieshouse.company_appointments.interceptor;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class FullRecordAuthenticationInterceptor implements HandlerInterceptor {
    private final AuthenticationHelper authHelper;
    private final Logger logger;

    @Autowired
    public FullRecordAuthenticationInterceptor(AuthenticationHelper authHelper, Logger logger) {
        this.authHelper = authHelper;
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String identityType = authHelper.getAuthorisedIdentityType(request);
        Map<String, Object> logMap = new HashMap<>();

        if (authHelper.isOauth2IdentityType(identityType)) {
            String companyNumber = request.getRequestURI().split("/")[2];

            if (authHelper.isTokenProtectedAndCompanyAuthorised(request, companyNumber)) {
                return true;
            } else {
                logger.infoRequest(request, "User not authorised. Token has insufficient permissions.", logMap);
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        }

        if (!authHelper.isApiKeyIdentityType(identityType)) {
            logMap.put("identityType", identityType);
            logger.infoRequest(request, "User not authorised. Identity type not correct", logMap);

            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        if (!authHelper.isKeyElevatedPrivilegesAuthorised(request)) {
            logMap.put("privileges", Arrays.asList(authHelper.getApiKeyPrivileges(request)));
            logger.infoRequest(request,
                    "User not authorised. API key does not have sufficient privileges.",
                    logMap);

            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }

}
