package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.CONVERTED_CLOSED;
import static uk.gov.companieshouse.company_appointments.model.data.CompanyStatus.DISSOLVED;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Component
class FilterService {

    private static final String ACTIVE = "active";
    private static final List<String> INACTIVE_STATUSES = List.of(DISSOLVED.getStatus(), CONVERTED_CLOSED.getStatus(),
            CLOSED.getStatus());

    Filter prepareFilter(String filter, String officerId) {
        if (StringUtils.isNotBlank(filter)) {
            if (ACTIVE.equals(filter)) {
                return new Filter(true, INACTIVE_STATUSES);
            } else {
                throw new BadRequestException(
                        "Invalid filter parameter supplied: %s, officer ID: %s".formatted(filter, officerId));
            }
        } else {
            return new Filter(false, emptyList());
        }
    }

    Optional<CompanyAppointmentDocument> findFirstActiveAppointment(List<CompanyAppointmentDocument> documents) {
        return documents.stream()
                .filter(document -> !INACTIVE_STATUSES.contains(document.getCompanyStatus()))
                .filter(document -> document.getData().getResignedOn() == null)
                .findFirst();
    }
}
