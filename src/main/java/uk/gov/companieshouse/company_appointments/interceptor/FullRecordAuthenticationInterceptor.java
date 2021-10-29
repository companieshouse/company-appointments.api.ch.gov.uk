package uk.gov.companieshouse.company_appointments.interceptor;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class FullRecordAuthenticationInterceptor implements HandlerInterceptor {
    private AuthenticationHelper authHelper;

    @Autowired
    public FullRecordAuthenticationInterceptor(AuthenticationHelper authHelper) {
        this.authHelper = authHelper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String identityType = authHelper.getAuthorisedIdentityType(request);
        boolean shouldAllow = true;

        if (!(authHelper.isApiKeyIdentityType(identityType) && authHelper.isKeyElevatedPrivilegesAuthorised(request))) {
            shouldAllow = false;
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        }

        return shouldAllow;
    }

}
