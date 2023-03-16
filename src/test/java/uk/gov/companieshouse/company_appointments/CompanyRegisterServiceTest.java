package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.ApiResponse;

import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.service.CompanyRegisterService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyRegisterServiceTest {

    @Mock
    CompanyMetricsApiService apiClientService;

    private CompanyRegisterService companyRegisterService;

    private final static String PUBLIC_REGISTER = "public-register";

    private final static String COMPANY_NUMBER = "123456";

    @BeforeEach
    void setUp() {
        companyRegisterService = new CompanyRegisterService(apiClientService);
    }

    @Test
    void whenRegisterTypeIsDirectorsAndIsStoredAtCompaniesHouseReturnTrue() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().getDirectors().setRegisterMovedTo(PUBLIC_REGISTER);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", COMPANY_NUMBER);
        assertTrue(result);
    }

    @Test
    void whenRegisterTypeIsSecretariesAndIsStoredAtCompaniesHouseReturnTrue() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().getSecretaries().setRegisterMovedTo(PUBLIC_REGISTER);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", COMPANY_NUMBER);
        assertTrue(result);
    }

    @Test
    void whenRegisterTypeIsLlpMembersAndIsStoredAtCompaniesHouseReturnTrue() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().getLlpMembers().setRegisterMovedTo(PUBLIC_REGISTER);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("llp_members", COMPANY_NUMBER);
        assertTrue(result);
    }

    @Test
    void whenRegisterTypeIsDirectorsAndIsNotStoredAtCompaniesHouseReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().getDirectors().setRegisterMovedTo("elsewhere");

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", COMPANY_NUMBER);
        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsSecretariesAndIsNotStoredAtCompaniesHouseReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().getSecretaries().setRegisterMovedTo("elsewhere");

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", COMPANY_NUMBER);
        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsLlpMembersAndIsNotStoredAtCompaniesHouseReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().getLlpMembers().setRegisterMovedTo("elsewhere");

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("llp_members", COMPANY_NUMBER);
        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsDirectorsAndThereAreNoRegistersForDirectorsReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().setDirectors(null);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsSecretariesAndThereAreNoRegistersForSecretariesReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().setSecretaries(null);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsLlpMembersAndThereAreNoRegistersForLlpMembersReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.getRegisters().setLlpMembers(null);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("llp_members", COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsDirectorAndThereAreNoRegistersReturnFalse() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        metricsApi.setRegisters(null);

        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsNullThrowBadRequestException() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));

        assertThrows(BadRequestException.class,
                () -> {
                    boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse(null, COMPANY_NUMBER);
                });
    }

    @Test
    void whenRegisterTypeIsIncorrectThrowBadRequestException() throws Exception {
        MetricsApi metricsApi = getTestResponse();
        when(apiClientService.invokeGetMetricsApi(COMPANY_NUMBER)).thenReturn(new ApiResponse<>(200, null, metricsApi));

        assertThrows(BadRequestException.class,
                () -> {
                    boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("incorrect", COMPANY_NUMBER);
                });
    }

    private MetricsApi getTestResponse() {
        MetricsApi metricsApi = new MetricsApi();
        RegistersApi registersApi = new RegistersApi();
        registersApi.setDirectors(new RegisterApi());
        registersApi.setSecretaries(new RegisterApi());
        registersApi.setLlpMembers(new RegisterApi());
        metricsApi.setRegisters(registersApi);

        return metricsApi;
    }
}
