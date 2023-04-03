package uk.gov.companieshouse.company_appointments.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DeltaDateValidator {

    public boolean isDeltaStale(final String incomingDelta, final String existingDelta) {
        return StringUtils.compare(incomingDelta, existingDelta) <= 0;
    }
}
