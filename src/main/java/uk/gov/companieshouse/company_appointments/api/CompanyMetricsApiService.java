package uk.gov.companieshouse.company_appointments.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyMetricsApiService {

    private final String metricsUrl;

    private final ApiClientService apiClientService;

    private final Logger logger;

    public CompanyMetricsApiService(@Value("${company-metrics-api.endpoint}") String metricsUrl,
                                  Logger logger, ApiClientService apiClientService) {
        this.metricsUrl = metricsUrl;
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    public ApiResponse<MetricsApi> invokeGetMetricsApi(String companyNumber)
            throws ServiceUnavailableException, NotFoundException {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        internalApiClient.setBasePath(metricsUrl);

        PrivateCompanyMetricsGet companyMetricsGet =
                internalApiClient.privateCompanyMetricsResourceHandler().getCompanyMetrics(String.format("/company/%s/metrics", companyNumber));

        return handleApiCall(companyMetricsGet, companyNumber);
    }

    private ApiResponse<MetricsApi> handleApiCall(PrivateCompanyMetricsGet companyMetricsGet, String companyNumber)
            throws ServiceUnavailableException, NotFoundException {
        try {
            return companyMetricsGet.execute();
        } catch (ApiErrorResponseException exception) {
            if (exception.getStatusCode() == 404) {
                logger.error(String.format("Metrics not found for company number %s", companyNumber), exception, DataMapHolder.getLogMap());
                throw new NotFoundException(exception.getMessage());
            } else {
                logger.error("Error occurred while calling /company-metrics endpoint", exception, DataMapHolder.getLogMap());
                throw new ServiceUnavailableException(exception.getMessage());
            }
        } catch (Exception exception) {
            logger.error("Error occurred while calling /company-metrics endpoint", exception, DataMapHolder.getLogMap());
            throw new ServiceUnavailableException(exception.getMessage());
        }
    }
}
