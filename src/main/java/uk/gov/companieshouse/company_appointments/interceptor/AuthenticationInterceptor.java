package uk.gov.companieshouse.company_appointments.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.gov.companieshouse.logging.Logger;

public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    private Logger logger;

    @Autowired
    public AuthenticationInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (request.getHeader("ERIC-Identity") == null || request.getHeader("ERIC-Identity-Type") == null) {
            response.setStatus(401);
            return false;
        }

        return true;
    }

}
