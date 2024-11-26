package uk.gov.companieshouse.company_appointments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;

@SpringBootTest
class ResourceChangedApiServiceAspectFeatureFlagDisabledITest {

    private final ApiResponse<Void> response = new ApiResponse<>(200, null, null);

    @InjectMocks
    private ResourceChangedApiService resourceChangedApiService;

    @Mock
    private ResourceChangedRequestMapper mapper;
    @Mock
    private Supplier<InternalApiClient> chsKafkaApiClient;
    @Mock
    private InternalApiClient client;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;
    @Mock
    private ChangedResource changedResource;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;


    @Test
    void testThatKafkaApiShouldBeCalledWhenFeatureFlagDisabled()
            throws ApiErrorResponseException, ServiceUnavailableException {
        when(chsKafkaApiClient.get()).thenReturn(client);
        when(client.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(mapper.mapChangedResource(any())).thenReturn(changedResource);
        when(changedResourcePost.execute()).thenReturn(response);

        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        verify(privateChangedResourceHandler).postChangedResource("/private/resource-changed", changedResource);
        verify(mapper).mapChangedResource(resourceChangedRequest);
    }
}
