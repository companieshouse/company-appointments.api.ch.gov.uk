package uk.gov.companieshouse.company_appointments.service;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.logging.Logger;

@Aspect
@Component
public class CompanyAppointmentsFullRecordServiceAspectDecorator {

    private final CompanyProfileClient companyProfileClient;
    private final Logger logger;

    public CompanyAppointmentsFullRecordServiceAspectDecorator(CompanyProfileClient companyProfileClient, Logger logger) {
        this.companyProfileClient = companyProfileClient;
        this.logger = logger;
    }

    @AfterReturning("@annotation(AddCompanyNameAndStatus)")
    public void populateCompanyNameAndCompanyStatusFields(Object returnValue) throws ApiErrorResponseException, NotFoundException, URIValidationException {
        if (!(returnValue instanceof CompanyAppointmentDocument)) {
            return; // TODO: Return may stop execution of transform method in DeltaAppointmentTransformer so may need to change this
        }
        CompanyAppointmentDocument document = (CompanyAppointmentDocument) returnValue; // TODO: Is this the correct way of doing this?

        Data companyProfileData = companyProfileClient.getCompanyProfile(document.getCompanyNumber());

        String companyName = companyProfileData.getCompanyName();
        String companyStatus = companyProfileData.getCompanyStatus();
        String appointmentId = document.getAppointmentId();

        document.setCompanyName(companyName);
        document.setCompanyStatus(companyStatus);
        logger.debug(String.format("Company name [%s] and company status [%s] set on appointment [%s]", companyName, companyStatus, appointmentId));
    }
}
