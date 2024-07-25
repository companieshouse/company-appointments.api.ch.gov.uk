package uk.gov.companieshouse.company_appointments.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.metrics.PrivateCompanyMetricsResourceHandler;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class CompanyMetricsApiServiceTest {

    private static final String COMPANY_NUMBER = "12345678";

    private CompanyMetricsApiService service;

    @Mock
    ApiClientService apiClientService;
    @Mock
    InternalApiClient internalApiClient;
    @Mock
    PrivateCompanyMetricsResourceHandler handler;
    @Mock
    PrivateCompanyMetricsGet get;
    @Mock
    Logger logger;
    @Mock
    private HttpClient httpClient;


    @BeforeEach
    void setup() {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyMetricsResourceHandler()).thenReturn(handler);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(handler.getCompanyMetrics(anyString())).thenReturn(get);

        service = new CompanyMetricsApiService("url", logger, apiClientService);
    }

    @Test
    void whenApiReturnsCorrectlyThenReturnCompanyMetrics() throws Exception {
        MetricsApi api = new MetricsApi();

        when(get.execute()).thenReturn(new ApiResponse<>(200, null, api));

        ApiResponse<MetricsApi> response = service.invokeGetMetricsApi(COMPANY_NUMBER);

        assertEquals(api, response.getData());
    }

    @Test
    void whenApiReturnsNon200StatusThenThrowServiceUnavailableException() throws Exception {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(400,
                "statusMessage", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(get.execute()).thenThrow(apiErrorResponseException);

        assertThrows(ServiceUnavailableException.class,
                () -> service.invokeGetMetricsApi(COMPANY_NUMBER));
    }

    @Test
    void whenApiReturns200StatusWithExceptionThenThrowServiceUnavailableException() throws Exception {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(200,
                "statusMessage", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(get.execute()).thenThrow(apiErrorResponseException);

        assertThrows(ServiceUnavailableException.class,
                () -> service.invokeGetMetricsApi(COMPANY_NUMBER));
    }

    @Test
    void whenApiThrowsExceptionThenThrowServiceUnavailableException() throws Exception {
        URIValidationException uriValidationException = new URIValidationException("message");
        when(get.execute()).thenThrow(uriValidationException);

        assertThrows(ServiceUnavailableException.class,
                () -> service.invokeGetMetricsApi(COMPANY_NUMBER));
    }

    @Test
    void whenApiReturns404StatusThenReturn200OK() throws ApiErrorResponseException, URIValidationException {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(404,
                "statusMessage", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(get.execute()).thenThrow(apiErrorResponseException);

        ApiResponse<MetricsApi> response = service.invokeGetMetricsApi(COMPANY_NUMBER);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }
}
