package uk.gov.companieshouse.company_appointments.exception;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }
}
