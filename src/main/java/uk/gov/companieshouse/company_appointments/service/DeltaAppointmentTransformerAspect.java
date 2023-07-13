package uk.gov.companieshouse.company_appointments.service;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "seeding_collection_enabled")
public class DeltaAppointmentTransformerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
    private static final String COMPANY_PROFILE_NOT_FOUND = "Company profile not found";

    private final CompanyProfileClient companyProfileClient;

    public DeltaAppointmentTransformerAspect(CompanyProfileClient companyProfileClient) {
        this.companyProfileClient = companyProfileClient;
    }

    @AfterReturning(pointcut = "@annotation(AddCompanyNameAndStatus)", returning = "returnValue")
    public void populateCompanyNameAndCompanyStatusFields(Object returnValue) throws URIValidationException, FailedToTransformException {
        if (!(returnValue instanceof CompanyAppointmentDocument)) {
            return;
        }
        CompanyAppointmentDocument document = (CompanyAppointmentDocument) returnValue;

        try {
            companyProfileClient.getCompanyProfile(document.getCompanyNumber())
                    .ifPresentOrElse(data -> {
                                String companyName = data.getCompanyName();
                                String companyStatus = data.getCompanyStatus();
                                String appointmentId = document.getAppointmentId();

                                document.setCompanyName(companyName);
                                document.setCompanyStatus(companyStatus);

                                LOGGER.debug(String.format("Company name [%s] and company status [%s] set on appointment [%s]", companyName, companyStatus, appointmentId));
                            },
                            () -> {
                                throw new IllegalArgumentException(COMPANY_PROFILE_NOT_FOUND);
                            });
        } catch (NotFoundException e) {
            throw new IllegalArgumentException(COMPANY_PROFILE_NOT_FOUND, e);
        } catch (ServiceUnavailableException e) {
            throw new FailedToTransformException(e.getMessage());
        }
    }
}
