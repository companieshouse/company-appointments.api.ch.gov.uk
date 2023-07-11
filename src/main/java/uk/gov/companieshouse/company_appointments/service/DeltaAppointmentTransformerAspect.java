package uk.gov.companieshouse.company_appointments.service;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
public class DeltaAppointmentTransformerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private final CompanyProfileClient companyProfileClient;

    public DeltaAppointmentTransformerAspect(CompanyProfileClient companyProfileClient) {
        this.companyProfileClient = companyProfileClient;
    }

    @AfterReturning(pointcut="@annotation(AddCompanyNameAndStatus)", returning="returnValue")
    public void populateCompanyNameAndCompanyStatusFields(Object returnValue) throws NotFoundException, URIValidationException, ServiceUnavailableException {
        if (!(returnValue instanceof CompanyAppointmentDocument)) {
            return;
        }
        CompanyAppointmentDocument document = (CompanyAppointmentDocument) returnValue;

        Data companyProfileData = companyProfileClient.getCompanyProfile(document.getCompanyNumber())
                .orElseThrow(() -> new NotFoundException("Company profile record not found"));

        String companyName = companyProfileData.getCompanyName();
        String companyStatus = companyProfileData.getCompanyStatus();
        String appointmentId = document.getAppointmentId();

        document.setCompanyName(companyName);
        document.setCompanyStatus(companyStatus);
        LOGGER.debug(String.format("Company name [%s] and company status [%s] set on appointment [%s]", companyName, companyStatus, appointmentId));
    }
}
