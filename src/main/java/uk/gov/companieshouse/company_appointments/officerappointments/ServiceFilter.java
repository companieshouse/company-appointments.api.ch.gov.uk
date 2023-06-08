package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.function.ToIntFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;

@Component
class ServiceFilter {

    private static final String REMOVED = "removed";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private static final String DISSOLVED = "dissolved";
    private static final String ACTIVE = "active";
    private static final ToIntFunction<AppointmentCounts> ACTIVE_COUNT_UNFILTERED = counts -> counts.getTotalCount() -
            counts.getInactiveCount() - counts.getResignedCount();
    private static final ToIntFunction<AppointmentCounts> ACTIVE_COUNT_FILTERED = AppointmentCounts::getTotalCount;

    Filter prepareFilter(String filter, String officerId) throws BadRequestException {
        if (StringUtils.isNotBlank(filter)) {
            if (ACTIVE.equals(filter)) {
                return new Filter(true, ACTIVE_COUNT_FILTERED, List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED));
            } else {
                throw new BadRequestException(
                        String.format("Invalid filter parameter supplied: %s, officer ID: %s",
                                filter, officerId));
            }
        } else {
            return new Filter(false, ACTIVE_COUNT_UNFILTERED, emptyList());
        }
    }
}
