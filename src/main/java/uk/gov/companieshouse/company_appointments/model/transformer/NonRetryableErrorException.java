package uk.gov.companieshouse.company_appointments.model.transformer;

/**
 * Thrown to indicate a non-recoverable error in processing that is futile to be tried again. An example of a
 * non-recoverable error is a data conversion error (e.g. bad date/time value).
 */
public class NonRetryableErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NonRetryableErrorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public NonRetryableErrorException(Exception exception) {
        super(exception);
    }


}
