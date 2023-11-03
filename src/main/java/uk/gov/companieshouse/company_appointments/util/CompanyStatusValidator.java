package uk.gov.companieshouse.company_appointments.util;

import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.model.data.CompanyStatus;

@Component
public class CompanyStatusValidator {
    public boolean isValidCompanyStatus(String requestCompanyStatus) {
        return Stream.of(CompanyStatus.values()).anyMatch(status -> status.getStatus().equals(requestCompanyStatus));
    }
}
