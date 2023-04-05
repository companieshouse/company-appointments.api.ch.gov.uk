package uk.gov.companieshouse.company_appointments.interceptor;

import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestLoggingInterceptor implements HandlerInterceptor, RequestLogger {

    private Logger logger;

    @Autowired
    public RequestLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logStartRequestProcessing(request, logger);
        Optional.ofNullable(request.getHeader("x-request-id"))
                .ifPresentOrElse(header -> MDC.put(REQUEST_ID.value(), header),
                        () -> MDC.remove(REQUEST_ID.value()));

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        logEndRequestProcessing(request, response, logger);
        MDC.remove(REQUEST_ID.value());
    }
}
