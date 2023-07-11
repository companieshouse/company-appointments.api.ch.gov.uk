package uk.gov.companieshouse.company_appointments.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class CompanyProfileClient {

    private final Supplier<InternalApiClient> internalApiClientSupplier;

    public CompanyProfileClient(Supplier<InternalApiClient> internalApiClientSupplier) {
        this.internalApiClientSupplier = internalApiClientSupplier;
    }

    public Optional<Data> getCompanyProfile(String companyNumber) throws URIValidationException, NotFoundException, ServiceUnavailableException {
        InternalApiClient client = internalApiClientSupplier.get();

        Optional<Data> companyProfileData = Optional.empty();
        try {
            companyProfileData = Optional.ofNullable(client.privateCompanyResourceHandler()
                    .getCompanyFullProfile(String.format("/company/%s", companyNumber))
                    .execute()
                    .getData());
        } catch (ApiErrorResponseException ex) {
            int statusCode = ex.getStatusCode();
            if (statusCode == 404) {
                throw new NotFoundException(String.format("No company profile record could be found for company number: [%s]", companyNumber));
            } else if (HttpStatus.valueOf(statusCode).is5xxServerError()) {
                throw new ServiceUnavailableException(String.format("Error connecting to company profile api with status code: [%s]", statusCode));
            }
        }
        return companyProfileData;
    }
}
