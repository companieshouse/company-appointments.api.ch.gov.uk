package uk.gov.companieshouse.company_appointments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;

@Service
public class CompanyRegisterService {
    private static final String PUBLIC_REGISTER = "public-register";

    private final CompanyMetricsApiService companyMetricsApiService;

    @Autowired
    public CompanyRegisterService(CompanyMetricsApiService companyMetricsApiService) {
        this.companyMetricsApiService = companyMetricsApiService;
    }

    public boolean isRegisterHeldInCompaniesHouse (String registerType, String companyNumber) throws ServiceUnavailableException, BadRequestException {
        RegistersApi registersApi = companyMetricsApiService.invokeGetMetricsApi(companyNumber).getData().getRegisters();

        if (registerType == null) {
            throw new BadRequestException("If registerView is true then registerType must be set");
        } else if (registerType.equals("directors")) {
            return registersApi != null && registersApi.getDirectors() != null && registersApi.getDirectors().getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else if (registerType.equals("secretaries")) {
            return registersApi != null && registersApi.getSecretaries() != null && registersApi.getSecretaries().getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else if (registerType.equals("llp_members")) {
            return registersApi != null && registersApi.getLlpMembers() != null && registersApi.getLlpMembers().getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else {
            throw new BadRequestException("Incorrect register type, must be directors, secretaries or llp_members");
        }
    }


}
