package uk.gov.companieshouse.company_appointments.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable exception) {
        super(message, exception);
    }

}
