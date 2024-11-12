package uk.gov.companieshouse.company_appointments.api;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;

@Component
public class CompanyMetricsApiClientFactory implements Supplier<InternalApiClient>, ApiClientFactory {

    private final String chsApiKey;
    private final String metricsUrl;

    public CompanyMetricsApiClientFactory(@Value("${chs.kafka.api.key}") String chsApiKey,
            @Value("${company-metrics-api.endpoint}") String metricsUrl) {
        this.chsApiKey = chsApiKey;
        this.metricsUrl = metricsUrl;
    }

    @Override
    public InternalApiClient get() {
        ApiKeyHttpClient apiKeyHttpClient = new ApiKeyHttpClient(chsApiKey);
        apiKeyHttpClient.setRequestId(DataMapHolder.getRequestId());

        InternalApiClient internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setBasePath(metricsUrl);

        return internalApiClient;
    }
}
