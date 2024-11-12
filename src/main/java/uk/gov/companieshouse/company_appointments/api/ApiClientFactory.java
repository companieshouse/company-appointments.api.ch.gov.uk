package uk.gov.companieshouse.company_appointments.api;

import uk.gov.companieshouse.api.InternalApiClient;

/**
 * The {@code ApiClientFactory} interface provides an abstraction that can be used when testing {@code ApiClientManager}
 * static methods, without imposing the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientFactory {

    InternalApiClient get();
}
