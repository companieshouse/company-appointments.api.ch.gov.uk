package uk.gov.companieshouse.company_appointments.logging;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;
import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.RequestLogger;

@Component
@Order(value = HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter implements RequestLogger {

    private static final String HEALTHCHECK_PATH = "/healthcheck";
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        logStartRequestProcessing(request, LOGGER);
        DataMapHolder.initialise(Optional
                .ofNullable(request.getHeader(REQUEST_ID.value()))
                .orElse(UUID.randomUUID().toString()));
        try {
            filterChain.doFilter(request, response);
        } finally {
            logEndRequestProcessing(request, response, LOGGER);
            DataMapHolder.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HEALTHCHECK_PATH.equals(request.getRequestURI());
    }
}
