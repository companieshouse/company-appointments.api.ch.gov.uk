package uk.gov.companieshouse.company_appointments.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
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

    public ApiResponse<MetricsApi> invokeGetMetricsApi(String companyNumber) throws ServiceUnavailableException {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.setBasePath(metricsUrl);

        PrivateCompanyMetricsGet companyMetricsGet =
                internalApiClient.privateCompanyMetricsResourceHandler().getCompanyMetrics(String.format("/company/%s/metrics", companyNumber));

        return handleApiCall(companyMetricsGet);
    }

    private ApiResponse<MetricsApi> handleApiCall(PrivateCompanyMetricsGet companyMetricsGet) throws ServiceUnavailableException {
        try {
            return companyMetricsGet.execute();
        } catch (ApiErrorResponseException exp) {
            HttpStatus statusCode = HttpStatus.valueOf(exp.getStatusCode());
            if (!statusCode.is2xxSuccessful()) {
                logger.error("Unsuccessful call to /company-metrics endpoint", exp);
            } else {
                logger.error("Error occurred while calling /company-metrics endpoint", exp);
            }
            throw new ServiceUnavailableException(exp.getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while calling /company-metrics endpoint", e);
            throw new ServiceUnavailableException(e.getMessage());
        }
    }
}
