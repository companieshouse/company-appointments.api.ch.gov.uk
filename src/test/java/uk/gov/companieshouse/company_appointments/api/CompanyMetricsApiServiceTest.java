package uk.gov.companieshouse.company_appointments.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import java.util.stream.Stream;
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
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.metrics.PrivateCompanyMetricsResourceHandler;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class CompanyMetricsApiServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String METRICS_URI = "/company/%s/metrics".formatted(COMPANY_NUMBER);
    private static final MetricsApi METRICS_RESPONSE_BODY = new MetricsApi();
    private static final ApiResponse<MetricsApi> SUCCESS_RESPONSE = new ApiResponse<>(200, null, METRICS_RESPONSE_BODY);

    @InjectMocks
    private CompanyMetricsApiService service;

    @Mock
    private Supplier<InternalApiClient> metricsApiClient;

    @Mock
    private InternalApiClient client;
    @Mock
    private PrivateCompanyMetricsResourceHandler companyMetricsResourceHandler;
    @Mock
    private PrivateCompanyMetricsGet privateCompanyMetricsGet;
    @Mock
    private ApiErrorResponseException apiErrorResponseException;

    @Test
    void shouldReturnSuccessResponseFromMetricsApi() throws Exception {
        // given
        when(metricsApiClient.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(companyMetricsResourceHandler);
        when(companyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenReturn(SUCCESS_RESPONSE);

        // when
        ApiResponse<MetricsApi> actual = service.invokeGetMetricsApi(COMPANY_NUMBER);

        // then
        assertEquals(SUCCESS_RESPONSE, actual);
        verify(companyMetricsResourceHandler).getCompanyMetrics(METRICS_URI);
    }

    @ParameterizedTest
    @MethodSource("apiErrorResponseScenarios")
    void shouldCatchApiErrorResponseExceptionAndThrowBadGatewayException(final int statusCode,
            Class<RuntimeException> expectedThrownException) throws Exception {
        // given
        when(metricsApiClient.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(companyMetricsResourceHandler);
        when(companyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(apiErrorResponseException);
        when(apiErrorResponseException.getStatusCode()).thenReturn(statusCode);

        // when
        Executable executable = () -> service.invokeGetMetricsApi(COMPANY_NUMBER);

        // then
        assertThrows(expectedThrownException, executable);
        verify(companyMetricsResourceHandler).getCompanyMetrics(METRICS_URI);
    }

    @Test
    void shouldCatchURIValidationExceptionAndThrowBadGatewayException() throws Exception {
        // given
        when(metricsApiClient.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(companyMetricsResourceHandler);
        when(companyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(URIValidationException.class);

        // when
        Executable executable = () -> service.invokeGetMetricsApi(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(companyMetricsResourceHandler).getCompanyMetrics(METRICS_URI);
    }

    private static Stream<Arguments> apiErrorResponseScenarios() {
        return Stream.of(
                Arguments.of(404, NotFoundException.class),
                Arguments.of(503, BadGatewayException.class));
    }
}
