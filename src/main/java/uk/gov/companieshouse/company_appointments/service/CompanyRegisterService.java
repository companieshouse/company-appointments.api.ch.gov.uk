package uk.gov.companieshouse.company_appointments.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;

@Service
public class CompanyRegisterService {

    private static final String PUBLIC_REGISTER = "public-register";

    public boolean isRegisterHeldInCompaniesHouse(String registerType, RegistersApi registersApi) {

        if (registerType == null) {
            throw new BadRequestException("If registerView is true then registerType must be set");
        } else if (registerType.equals("directors")) {
            return registersApi != null && registersApi.getDirectors() != null && registersApi.getDirectors()
                    .getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else if (registerType.equals("secretaries")) {
            return registersApi != null && registersApi.getSecretaries() != null && registersApi.getSecretaries()
                    .getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else if (registerType.equals("llp_members")) {
            return registersApi != null && registersApi.getLlpMembers() != null && registersApi.getLlpMembers()
                    .getRegisterMovedTo().equals(PUBLIC_REGISTER);
        } else {
            throw new BadRequestException("Incorrect register type, must be directors, secretaries or llp_members");
        }
    }
}
