package uk.gov.companieshouse.company_appointments.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;

@ExtendWith(MockitoExtension.class)
class ResourceChangedApiServiceTest {

    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";

    @InjectMocks
    private ResourceChangedApiService resourceChangedApiService;

    @Mock
    private InternalApiClient chsKafkaApiClient;
    @Mock
    private ResourceChangedRequestMapper mapper;

    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost privateChangedResourcePost;
    @Mock
    private ChangedResource changedResource;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;

    @Test
    void shouldSuccessfullyInvokeChsKafkaApi() throws Exception {
        // given
        when(chsKafkaApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(anyString(), any())).thenReturn(
                privateChangedResourcePost);
        when(mapper.mapChangedResource(any())).thenReturn(changedResource);

        // when
        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        // then
        verify(privateChangedResourceHandler).postChangedResource(CHANGED_RESOURCE_URI, changedResource);
        verify(mapper).mapChangedResource(resourceChangedRequest);
        verify(privateChangedResourcePost).execute();
    }

    @Test
    void shouldThrowBadGatewayOnApiErrorResponseException() throws Exception {
        // given
        when(chsKafkaApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(anyString(), any())).thenReturn(
                privateChangedResourcePost);
        when(mapper.mapChangedResource(any())).thenReturn(changedResource);
        when(privateChangedResourcePost.execute()).thenThrow(ApiErrorResponseException.class);

        // when
        Executable executable = () -> resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privateChangedResourceHandler).postChangedResource(CHANGED_RESOURCE_URI, changedResource);
        verify(mapper).mapChangedResource(resourceChangedRequest);
    }
}
