package uk.gov.companieshouse.company_appointments.api;

import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResourceChangedApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";

    private final ApiClientFactory chsKafkaApiClientFactory;
    private final ResourceChangedRequestMapper mapper;

    public ResourceChangedApiService(ApiClientFactory chsKafkaApiClientFactory,
            ResourceChangedRequestMapper mapper) {
        this.chsKafkaApiClientFactory = chsKafkaApiClientFactory;
        this.mapper = mapper;
    }

    @StreamEvents
    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) {
        InternalApiClient internalApiClient = chsKafkaApiClientFactory.get();

        try {
            return internalApiClient.privateChangedResourceHandler()
                    .postChangedResource(CHANGED_RESOURCE_URI, mapper.mapChangedResource(resourceChangedRequest))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            LOGGER.info("Resource changed call failed with status code [%s]".formatted(ex.getStatusCode()),
                    DataMapHolder.getLogMap());
            throw new BadGatewayException("Error calling resource changed endpoint", ex);
        }
    }
}
