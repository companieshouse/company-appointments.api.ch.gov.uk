package uk.gov.companieshouse.company_appointments.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.logging.Logger;

@Service
public class ResourceChangedApiService {

    private static final String CHANGED_RESOURCE_URI = "/resource-changed";
    private final Logger logger;
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;
    private final ResourceChangedRequestMapper mapper;

    /**
     * Invoke API.
     */
    public ResourceChangedApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
                                ApiClientService apiClientService,
                                Logger logger,
                                ResourceChangedRequestMapper mapper) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.mapper = mapper;
    }


    /**
     * Calls the CHS Kafka api.
     * @param resourceChangedRequest encapsulates details relating to the updated or deleted company exemption
     * @return The service status of the response from chs kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) throws ServiceUnavailableException {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient(); //NOSONAR
        internalApiClient.setBasePath(chsKafkaUrl);

        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapper.mapChangedResource(resourceChangedRequest));

        return handleApiCall(changedResourcePost);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) throws ServiceUnavailableException {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException ex) {
            if (!HttpStatus.valueOf(ex.getStatusCode()).is2xxSuccessful()) {
                logger.error("Unsuccessful call to /resource-changed endpoint", ex);
            } else {
                logger.error("Error occurred while calling /resource-changed endpoint", ex);
            }
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }
}
