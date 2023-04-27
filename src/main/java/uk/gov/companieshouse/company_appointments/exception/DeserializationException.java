package uk.gov.companieshouse.company_appointments.exception;

public class DeserializationException extends RuntimeException {

    public DeserializationException(String message, Exception ex) {
        super(message);
    }
}
