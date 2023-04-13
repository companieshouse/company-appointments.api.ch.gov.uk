package uk.gov.companieshouse.company_appointments.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;

@Component
public class ApiClientServiceImpl implements ApiClientService {

    @Value("${chs.kafka.api.key}") String chsApiKey;

    @Value("${chs.kafka.api.endpoint}") String internalApiUrl;


    @Override
    public InternalApiClient getInternalApiClient() {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient());
        internalApiClient.setInternalBasePath(internalApiUrl);
        internalApiClient.setBasePath(internalApiUrl);

        return internalApiClient;
    }

    private HttpClient getHttpClient() {
        return new ApiKeyHttpClient(chsApiKey);
    }
}
