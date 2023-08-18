package uk.gov.companieshouse.company_appointments.interceptor;

import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

public class RequestLoggingInterceptor implements HandlerInterceptor, RequestLogger {

    private final Logger logger;

    public RequestLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logStartRequestProcessing(request, logger);
        DataMapHolder.initialise(Optional
                .ofNullable(request.getHeader(REQUEST_ID.value()))
                .orElse(UUID.randomUUID().toString()));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        logEndRequestProcessing(request, response, logger);
        DataMapHolder.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        DataMapHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
