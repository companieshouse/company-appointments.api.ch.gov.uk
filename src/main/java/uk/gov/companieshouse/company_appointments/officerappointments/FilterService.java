package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static uk.gov.companieshouse.company_appointments.model.data.AcceptedCompanyStatuses.CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.AcceptedCompanyStatuses.CONVERTED_CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.AcceptedCompanyStatuses.DISSOLVED;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;

@Component
class FilterService {

    private static final String ACTIVE = "active";

    Filter prepareFilter(String filter, String officerId) {
        if (StringUtils.isNotBlank(filter)) {
            if (ACTIVE.equals(filter)) {
                return new Filter(true, List.of(DISSOLVED.name(), CONVERTED_CLOSED.name(), CLOSED.name()));
            } else {
                throw new BadRequestException(
                        String.format("Invalid filter parameter supplied: %s, officer ID: %s",
                                filter, officerId));
            }
        } else {
            return new Filter(false, emptyList());
        }
    }
}
