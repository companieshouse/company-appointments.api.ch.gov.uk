package uk.gov.companieshouse.company_appointments.util;

import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.model.data.AcceptedCompanyStatuses;

@Component
public class CompanyStatusValidator {
    public boolean isValidCompanyStatus(String requestCompanyStatus) {
        return Stream.of(AcceptedCompanyStatuses.values()).anyMatch(status -> status.getValidCompanyStatus().equals(requestCompanyStatus));
    }
}
