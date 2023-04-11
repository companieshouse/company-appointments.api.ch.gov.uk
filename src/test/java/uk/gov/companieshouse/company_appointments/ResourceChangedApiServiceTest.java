package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
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
    void invokeChsKafkaApi() throws ApiErrorResponseException, ServiceUnavailableException {
        // given
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);

        // when
        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        // then
        verify(apiClientService).getInternalApiClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource("/resource-changed", changedResource);
        verify(changedResourcePost).execute();
    }

    @ParameterizedTest
    @MethodSource("invokeChsKafkaApiExceptionFixtures")
    void invokeChsKafkaApiExceptionTests(ResourceChangedApiServiceTestArgument argument) throws ApiErrorResponseException {
        // given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(argument.getStatusCode(), argument.getErrorMessage(), new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(builder);

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);

        // when
        Executable executable = () -> resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource("/resource-changed", changedResource);
        verify(changedResourcePost, times(1)).execute();
    }

    private static Stream<Arguments> invokeChsKafkaApiExceptionFixtures() {
        return Stream.of(
                Arguments.of(
                        Named.of("Throws service unavailable exception when response code is HTTP 500",
                                new ResourceChangedApiServiceTestArgument(500, "Internal Service Error")
                        )
                ),
                Arguments.of(
                        Named.of("Throws service unavailable exception when response code is HTTP 503",
                                new ResourceChangedApiServiceTestArgument(503, "Service Unavailable")
                        )
                ),
                Arguments.of(
                        Named.of("Throws service unavailable exception when response code is HTTP 200 with errors",
                                new ResourceChangedApiServiceTestArgument(200, "")
                        )
                )
        );
    }

    private static class ResourceChangedApiServiceTestArgument {
        private final int statusCode;
        private final String errorMessage;

        public ResourceChangedApiServiceTestArgument(int statusCode, String errorMessage) {
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

}
