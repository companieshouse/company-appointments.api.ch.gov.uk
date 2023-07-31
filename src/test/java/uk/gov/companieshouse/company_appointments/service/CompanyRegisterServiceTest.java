package uk.gov.companieshouse.company_appointments.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;

@ExtendWith(MockitoExtension.class)
class CompanyRegisterServiceTest {

    private CompanyRegisterService companyRegisterService;

    private final static String PUBLIC_REGISTER = "public-register";

    @BeforeEach
    void setUp() {
        companyRegisterService = new CompanyRegisterService();
    }

    @Test
    void whenRegisterTypeIsDirectorsAndIsStoredAtCompaniesHouseReturnTrue() {
        RegistersApi registersApi = getTestResponse();
        registersApi.getDirectors().setRegisterMovedTo(PUBLIC_REGISTER);

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", registersApi);
        assertTrue(result);
    }

    @Test
    void whenRegisterTypeIsSecretariesAndIsStoredAtCompaniesHouseReturnTrue() {
        RegistersApi registersApi = getTestResponse();
        registersApi.getSecretaries().setRegisterMovedTo(PUBLIC_REGISTER);

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", registersApi);
        assertTrue(result);
    }

    @Test
    void whenRegisterTypeIsLlpMembersAndIsStoredAtCompaniesHouseReturnTrue() {
        RegistersApi registersApi = getTestResponse();
        registersApi.getLlpMembers().setRegisterMovedTo(PUBLIC_REGISTER);

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("llp_members", registersApi);
        assertTrue(result);
    }

    @Test
    void whenRegisterTypeIsDirectorsAndIsNotStoredAtCompaniesHouseReturnFalse() {
        RegistersApi registersApi = getTestResponse();
        registersApi.getDirectors().setRegisterMovedTo("elsewhere");

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", registersApi);
        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsSecretariesAndIsNotStoredAtCompaniesHouseReturnFalse() {
        RegistersApi registersApi = getTestResponse();
        registersApi.getSecretaries().setRegisterMovedTo("elsewhere");

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", registersApi);
        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsLlpMembersAndIsNotStoredAtCompaniesHouseReturnFalse() {
        RegistersApi registersApi = getTestResponse();
        registersApi.getLlpMembers().setRegisterMovedTo("elsewhere");

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("llp_members", registersApi);
        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsDirectorsAndThereAreNoRegistersForDirectorsReturnFalse() {
        RegistersApi registersApi = getTestResponse();
        registersApi.setDirectors(null);

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", registersApi);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsSecretariesAndThereAreNoRegistersForSecretariesReturnFalse() {
        RegistersApi registersApi = getTestResponse();
        registersApi.setSecretaries(null);

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("secretaries", registersApi);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsLlpMembersAndThereAreNoRegistersForLlpMembersReturnFalse() {
        RegistersApi registersApi = getTestResponse();
        registersApi.setLlpMembers(null);

        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("llp_members", registersApi);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsDirectorAndThereAreNoRegistersReturnFalse() {
        boolean result = companyRegisterService.isRegisterHeldInCompaniesHouse("directors", null);

        assertFalse(result);
    }

    @Test
    void whenRegisterTypeIsNullThrowBadRequestException() {
        RegistersApi registersApi = getTestResponse();

        assertThrows(BadRequestException.class,
                () -> companyRegisterService.isRegisterHeldInCompaniesHouse(null, registersApi));
    }

    @Test
    void whenRegisterTypeIsIncorrectThrowBadRequestException() {
        RegistersApi registersApi = getTestResponse();

        assertThrows(BadRequestException.class,
                () -> companyRegisterService.isRegisterHeldInCompaniesHouse("incorrect", registersApi));
    }

    private RegistersApi getTestResponse() {
        RegistersApi registersApi = new RegistersApi();
        registersApi.setDirectors(new RegisterApi());
        registersApi.setSecretaries(new RegisterApi());
        registersApi.setLlpMembers(new RegisterApi());

        return registersApi;
    }
}
