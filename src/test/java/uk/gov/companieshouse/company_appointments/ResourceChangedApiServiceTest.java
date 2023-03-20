package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.api.ApiClientService;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.model.util.ServiceStatus;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ResourceChangedApiServiceTest {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;

    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Mock
    private ApiResponse<Void> response;

    @Mock
    private Logger logger;

    @Mock
    private ResourceChangedRequestMapper mapper;

    @Mock
    private ResourceChangedRequest resourceChangedRequest;

    @Mock
    private ChangedResource changedResource;

    @InjectMocks
    private ResourceChangedApiService resourceChangedApiService;

    @Test
    @DisplayName("Test should successfully invoke chs-kafka-api")
    void invokeChsKafkaApi() throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);

        ServiceStatus serviceStatus = resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        assertEquals(serviceStatus.SUCCESS, serviceStatus);

        verify(apiClientService).getInternalApiClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource("/resource-changed", changedResource);
        verify(changedResourcePost).execute();
    }

    @Test
    @DisplayName("Test should handle a service unavailable exception when response code is HTTP 503")
    void invokeChsKafkaApi503() throws ApiErrorResponseException {
        setupExceptionScenario(503, "Service Unavailable");

        ServiceStatus serviceStatus = resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        assertEquals(ServiceStatus.SERVER_ERROR, serviceStatus);
        verifyExceptionScenario();
    }

    @Test
    @DisplayName("Test should handle a service unavailable exception when response code is HTTP 500")
    void invokeChsKafkaApi500() throws ApiErrorResponseException {
        setupExceptionScenario(500, "Internal Service Error");

        ServiceStatus serviceStatus = resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        assertEquals(ServiceStatus.SERVER_ERROR, serviceStatus);
        verifyExceptionScenario();
    }

    @Test
    @DisplayName("Test should handle a service unavailable exception when response code is HTTP 200 with errors")
    void invokeChsKafkaApi200Errors() throws ApiErrorResponseException {
        setupExceptionScenario(200, "");

        ServiceStatus serviceStatus = resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        assertEquals(ServiceStatus.SERVER_ERROR, serviceStatus);
        verifyExceptionScenario();
    }

    private void setupExceptionScenario(int statusCode, String statusMessage) throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);

        HttpResponseException.Builder builder = new HttpResponseException.Builder(statusCode,
                statusMessage, new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);
    }

    private void verifyExceptionScenario() throws ApiErrorResponseException {
        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource("/resource-changed", changedResource);
        verify(changedResourcePost, times(1)).execute();
    }
}
