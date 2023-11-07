package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.CONVERTED_CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.DISSOLVED;


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
                return new Filter(true, List.of(DISSOLVED.getStatus(), CONVERTED_CLOSED.getStatus(), CLOSED.getStatus()));
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
