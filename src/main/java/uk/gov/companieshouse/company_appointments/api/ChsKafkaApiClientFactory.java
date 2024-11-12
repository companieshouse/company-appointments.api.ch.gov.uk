package uk.gov.companieshouse.company_appointments.api;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;

@Component
public class ChsKafkaApiClientFactory implements Supplier<InternalApiClient>, ApiClientFactory {

    private final String chsApiKey;
    private final String internalApiUrl;

    public ChsKafkaApiClientFactory(@Value("${chs.kafka.api.key}") String chsApiKey,
            @Value("${chs.kafka.api.endpoint}") String internalApiUrl) {
        this.chsApiKey = chsApiKey;
        this.internalApiUrl = internalApiUrl;
    }

    @Override
    public InternalApiClient get() {
        ApiKeyHttpClient apiKeyHttpClient = new ApiKeyHttpClient(chsApiKey);
        apiKeyHttpClient.setRequestId(DataMapHolder.getRequestId());

        InternalApiClient internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setInternalBasePath(internalApiUrl);
        internalApiClient.setBasePath(internalApiUrl);

        return internalApiClient;
    }
}
