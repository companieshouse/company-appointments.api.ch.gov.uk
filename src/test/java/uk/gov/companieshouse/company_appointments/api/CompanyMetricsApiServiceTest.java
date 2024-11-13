package uk.gov.companieshouse.company_appointments.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.metrics.PrivateCompanyMetricsResourceHandler;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;

@ExtendWith(MockitoExtension.class)
class CompanyMetricsApiServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String METRICS_URI = "/company/%s/metrics".formatted(COMPANY_NUMBER);
    private static final MetricsApi METRICS_RESPONSE_BODY = new MetricsApi();
    private static final ApiResponse<MetricsApi> SUCCESS_RESPONSE = new ApiResponse<>(200, null, METRICS_RESPONSE_BODY);

    @InjectMocks
    private CompanyMetricsApiService service;

    @Mock
    private CompanyMetricsApiClientFactory companyMetricsApiClientFactory;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateCompanyMetricsResourceHandler companyMetricsResourceHandler;
    @Mock
    private PrivateCompanyMetricsGet privateCompanyMetricsGet;

    @Test
    void shouldReturnSuccessResponseFromMetricsApi() throws Exception {
        // given
        when(companyMetricsApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyMetricsResourceHandler()).thenReturn(companyMetricsResourceHandler);
        when(companyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenReturn(SUCCESS_RESPONSE);

        // when
        ApiResponse<MetricsApi> actual = service.invokeGetMetricsApi(COMPANY_NUMBER);

        // then
        assertEquals(SUCCESS_RESPONSE, actual);
        verify(companyMetricsResourceHandler).getCompanyMetrics(METRICS_URI);
    }

    @Test
    void shouldCatchApiErrorResponseExceptionAndThrowBadGatewayException() throws Exception {
        // given
        when(companyMetricsApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyMetricsResourceHandler()).thenReturn(companyMetricsResourceHandler);
        when(companyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(ApiErrorResponseException.class);

        // when
        Executable executable = () -> service.invokeGetMetricsApi(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(companyMetricsResourceHandler).getCompanyMetrics(METRICS_URI);
    }

    @Test
    void shouldCatchURIValidationExceptionAndThrowBadGatewayException() throws Exception {
        // given
        when(companyMetricsApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyMetricsResourceHandler()).thenReturn(companyMetricsResourceHandler);
        when(companyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(URIValidationException.class);

        // when
        Executable executable = () -> service.invokeGetMetricsApi(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(companyMetricsResourceHandler).getCompanyMetrics(METRICS_URI);
    }
}
