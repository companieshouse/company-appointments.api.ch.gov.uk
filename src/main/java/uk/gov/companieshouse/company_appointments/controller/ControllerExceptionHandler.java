package uk.gov.companieshouse.company_appointments.controller;

import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.ConflictException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Void> handleBadRequest() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Void> handleConflict() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build();
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Void> handleBadGateway(BadGatewayException ex) {
        LOGGER.info("Recoverable exception: %s".formatted(Arrays.toString(ex.getStackTrace())),
                DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .build();
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Void> handleServiceUnavailable() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnknownException(Exception ex) {
        LOGGER.error(ex.getClass().getName(), ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

}
