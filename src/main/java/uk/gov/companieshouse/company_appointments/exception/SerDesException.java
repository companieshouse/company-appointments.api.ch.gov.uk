package uk.gov.companieshouse.company_appointments.exception;

public class SerDesException extends RuntimeException {

    public SerDesException(String message, Throwable ex) {
        super(message, ex);
    }
}
