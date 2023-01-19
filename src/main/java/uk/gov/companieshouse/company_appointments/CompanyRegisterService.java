package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyRegisterService {
    private final String metricsUrl;
    private final Logger logger;

    private final ApiClientService apiClientService;

    private final static String PUBLIC_REGISTER = "public-register";

    @Autowired
    public CompanyRegisterService(@Value("${company-metrics-api.endpoint}") String metricsUrl,
                                  Logger logger, ApiClientService apiClientService) {
        this.metricsUrl = metricsUrl;
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    public boolean isRegisterHeldInCompaniesHouse (String registerType, String companyNumber) throws ServiceUnavailableException, BadRequestException {
        RegistersApi registersApi = invokeGetMetricsApi(companyNumber).getData().getRegisters();

        if (registerType == null) {
            throw new BadRequestException("If registerView is true then registerType must be set");
        } else if (registerType.equals("directors")) {
            return registersApi != null && registersApi.getDirectors() != null && registersApi.getDirectors().getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else if (registerType.equals("secretaries")) {
            return registersApi != null && registersApi.getSecretaries() != null && registersApi.getSecretaries().getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else if (registerType.equals("llp_members")) {
            return registersApi != null && registersApi.getLlpMembers() != null && registersApi.getLlpMembers().getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else {
            throw new BadRequestException("Incorrect register type, must be directors, secretaries or llp_members");
        }
    }

    private ApiResponse<MetricsApi> invokeGetMetricsApi(String companyNumber) throws ServiceUnavailableException {
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
                throw new ServiceUnavailableException(exp.getMessage());
            } else {
                logger.error("Error occurred while calling /company-metrics endpoint", exp);
                throw new RuntimeException(exp);
            }
        } catch (Exception e) {
            logger.error("Error occurred while calling /company-metrics endpoint", e);
            throw new RuntimeException(e);
        }
    }
}
