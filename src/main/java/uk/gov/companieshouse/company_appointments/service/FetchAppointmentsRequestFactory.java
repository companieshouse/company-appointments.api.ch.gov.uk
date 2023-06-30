package uk.gov.companieshouse.company_appointments.service;

import org.springframework.stereotype.Component;

@Component
public class FetchAppointmentsRequestFactory {
    public FetchAppointmentsRequest build() {
        return new FetchAppointmentsRequest();
    }
}
