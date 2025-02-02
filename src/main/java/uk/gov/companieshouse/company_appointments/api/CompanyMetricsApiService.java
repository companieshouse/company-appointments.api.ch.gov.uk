package uk.gov.companieshouse.company_appointments.api;

import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;

import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class CompanyMetricsApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final Supplier<InternalApiClient> metricsApiClient;

    public CompanyMetricsApiService(Supplier<InternalApiClient> metricsApiClient) {
        this.metricsApiClient = metricsApiClient;
    }

    public ApiResponse<MetricsApi> invokeGetMetricsApi(String companyNumber) {
        try {
            return metricsApiClient.get()
                    .privateCompanyMetricsResourceHandler()
                    .getCompanyMetrics(String.format("/company/%s/metrics", companyNumber))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            LOGGER.info("Company Metrics API call failed with status code [%s]".formatted(ex.getStatusCode()),
                    DataMapHolder.getLogMap());
            if (ex.getStatusCode() == 404) {
                throw new NotFoundException("Company Metrics API responded with 404 Not Found", ex);
            } else {
                throw new BadGatewayException("Error calling Company Metrics API endpoint", ex);
            }
        } catch (URIValidationException ex) {
            LOGGER.info("URI validation error when calling Company Metrics API", DataMapHolder.getLogMap());
            throw new BadGatewayException("URI validation error when calling Company Metrics API", ex);
        }
    }
}
