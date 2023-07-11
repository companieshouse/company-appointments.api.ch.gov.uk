package uk.gov.companieshouse.company_appointments;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.PrivateCompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyFullProfileGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.service.CompanyProfileClient;

import java.util.Collections;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyProfileClientTest {

    private static final String COMPANY_NUMBER = "12345678";

    @Mock
    private Supplier<InternalApiClient> internalApiClientSupplier;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateCompanyResourceHandler privateCompanyResourceHandler;
    @Mock
    private PrivateCompanyFullProfileGet privateCompanyFullProfileGet;
    @Mock
    private Data data;

    private CompanyProfileClient companyProfileClient;

    @BeforeEach
    void setUp() {
        companyProfileClient = new CompanyProfileClient(internalApiClientSupplier);
    }

    @Test
    @DisplayName("Successfully returns data")
    void getCompanyProfileData() throws Exception {
        // given
        Data expected = new Data();

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyResourceHandler()).thenReturn(privateCompanyResourceHandler);
        when(privateCompanyResourceHandler.getCompanyFullProfile(anyString())).thenReturn(privateCompanyFullProfileGet);
        when(privateCompanyFullProfileGet.execute()).thenReturn(new ApiResponse<>(HttpStatus.OK.value(), Collections.emptyMap(), expected));

        // when
        Data actual = companyProfileClient.getCompanyProfile(COMPANY_NUMBER).orElseThrow();

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Fails to return data when URI fails validation")
    void getCompanyProfileDataThrowsURIValidationException() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyResourceHandler()).thenReturn(privateCompanyResourceHandler);
        when(privateCompanyResourceHandler.getCompanyFullProfile(anyString())).thenReturn(privateCompanyFullProfileGet);
        when(privateCompanyFullProfileGet.execute()).thenThrow(URIValidationException.class);

        // when
        Executable executable = () -> companyProfileClient.getCompanyProfile("company number").orElseThrow();

        // then
        assertThrows(URIValidationException.class, executable);
    }

    @Test
    @DisplayName("Fails to return data after company number not found")
    void getCompanyProfileDataThrowsNotFoundException() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyResourceHandler()).thenReturn(privateCompanyResourceHandler);
        when(privateCompanyResourceHandler.getCompanyFullProfile(anyString())).thenReturn(privateCompanyFullProfileGet);
        when(privateCompanyFullProfileGet.execute())
                .thenThrow(ApiErrorResponseException
                        .fromHttpResponseException(
                                new HttpResponseException
                                        .Builder(HttpStatus.NOT_FOUND.value(), "", new HttpHeaders())
                                        .build()));

        // when
        Executable executable = () -> companyProfileClient.getCompanyProfile(COMPANY_NUMBER).orElseThrow();

        // then
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Fails to return data when company profile api is down")
    void getCompanyProfileDataThrowsServiceUnavailableException() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyResourceHandler()).thenReturn(privateCompanyResourceHandler);
        when(privateCompanyResourceHandler.getCompanyFullProfile(anyString())).thenReturn(privateCompanyFullProfileGet);
        when(privateCompanyFullProfileGet.execute())
                .thenThrow(ApiErrorResponseException
                        .fromHttpResponseException(
                                new HttpResponseException
                                        .Builder(HttpStatus.SERVICE_UNAVAILABLE.value(), "", new HttpHeaders())
                                        .build()));

        // when
        Executable executable = () -> companyProfileClient.getCompanyProfile(COMPANY_NUMBER).orElseThrow();

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    @DisplayName("Fails to return data after InternalServerError is returned")
    void getCompanyProfileDataThrowsInternalServerErrorException() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyResourceHandler()).thenReturn(privateCompanyResourceHandler);
        when(privateCompanyResourceHandler.getCompanyFullProfile(anyString())).thenReturn(privateCompanyFullProfileGet);
        when(privateCompanyFullProfileGet.execute())
                .thenThrow(ApiErrorResponseException
                        .fromHttpResponseException(
                                new HttpResponseException
                                        .Builder(HttpStatus.INTERNAL_SERVER_ERROR.value(), "", new HttpHeaders())
                                        .build()));

        // when
        Executable executable = () -> companyProfileClient.getCompanyProfile(COMPANY_NUMBER).orElseThrow();

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }
}
