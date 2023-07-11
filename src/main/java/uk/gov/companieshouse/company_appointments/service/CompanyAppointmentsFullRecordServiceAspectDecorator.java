package uk.gov.companieshouse.company_appointments.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
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
    // execution(* transform(..))

    @After(value = "@annotation(AddCompanyNameAndStatus)")
    public void populateCompanyNameAndCompanyStatusFields(JoinPoint joinPoint) throws ApiErrorResponseException, NotFoundException, URIValidationException, ServiceUnavailableException {
//        if (!(returnValue instanceof CompanyAppointmentDocument)) {
//            logger.error("Return value is not instance of CompanyAppointmentDocument");
//            return;
//        }
//        CompanyAppointmentDocument document = (CompanyAppointmentDocument) returnValue;
//
//        Data companyProfileData = companyProfileClient.getCompanyProfile(document.getCompanyNumber())
//                .orElseThrow(() -> new NotFoundException("Company profile record not found"));
//
//        String companyName = companyProfileData.getCompanyName();
//        String companyStatus = companyProfileData.getCompanyStatus();
//        String appointmentId = document.getAppointmentId();
//
//        document.setCompanyName(companyName);
//        document.setCompanyStatus(companyStatus);
//        logger.debug(String.format("Company name [%s] and company status [%s] set on appointment [%s]", companyName, companyStatus, appointmentId));
        System.out.println("TEST");
    }
}
