package uk.gov.companieshouse.company_appointments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;

@SpringBootTest
class ResourceChangedApiServiceAspectFeatureFlagDisabledITest {

    @Autowired
    private ResourceChangedApiService resourceChangedApiService;

    @MockBean
    private ChsKafkaApiClientFactory chsKafkaApiClientFactory;
    @MockBean
    private ResourceChangedRequestMapper mapper;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;
    @Mock
    private ChangedResource changedResource;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;
    @Mock
    private ApiResponse<Void> response;
    @Mock
    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
    }

    @Test
    void testThatKafkaApiShouldBeCalledWhenFeatureFlagDisabled()
            throws ApiErrorResponseException, ServiceUnavailableException {

        when(chsKafkaApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);

        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        verify(chsKafkaApiClientFactory).get();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource("/private/resource-changed",
                changedResource);
        verify(changedResourcePost).execute();
    }
}
