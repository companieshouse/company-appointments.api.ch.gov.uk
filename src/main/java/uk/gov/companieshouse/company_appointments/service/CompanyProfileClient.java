package uk.gov.companieshouse.company_appointments.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;

import java.util.function.Supplier;

@Component
public class CompanyProfileClient {

    private final Supplier<InternalApiClient> internalApiClientSupplier;

    public CompanyProfileClient(Supplier<InternalApiClient> internalApiClientSupplier) {
        this.internalApiClientSupplier = internalApiClientSupplier;
    }

    public Data getCompanyProfile(String companyNumber) throws ApiErrorResponseException, URIValidationException, NotFoundException {
        InternalApiClient client = internalApiClientSupplier.get();

        Data companyProfileData = new Data();
        try {
            companyProfileData = client.privateCompanyResourceHandler()
                    .getCompanyFullProfile(String.format("/company/%s", companyNumber))
                    .execute()
                    .getData();
        } catch (ApiErrorResponseException ex) {
            if (ex.getStatusCode() == 404) {
                throw new NotFoundException(ex.getMessage());
            } // TODO: Add appropriate catch blocks here
        }
        return companyProfileData;
    }
}
