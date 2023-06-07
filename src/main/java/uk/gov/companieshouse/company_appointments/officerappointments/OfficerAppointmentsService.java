package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;

@Service
public class OfficerAppointmentsService {

    private static final int ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE = 50;
    private static final int START_INDEX = 0;
    private static final String REMOVED = "removed";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private static final String DISSOLVED = "dissolved";
    private static final String ACTIVE = "active";

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;

    public OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    protected Optional<AppointmentList> getOfficerAppointments(
            OfficerAppointmentsRequest request) throws BadRequestException {
        String officerId = request.getOfficerId();

        Optional<CompanyAppointmentData> firstAppointment = repository.findFirstByOfficerId(officerId);
        if (firstAppointment.isEmpty()) {
            return Optional.empty();
        }

        int startIndex;
        if (request.getStartIndex() == null) {
            startIndex = START_INDEX;
        } else {
            startIndex = Math.abs(request.getStartIndex());
        }

        int itemsPerPage;
        if (request.getItemsPerPage() == null || request.getItemsPerPage() == 0) {
            itemsPerPage = ITEMS_PER_PAGE;
        } else if (Math.abs(request.getItemsPerPage()) > 50) {
            itemsPerPage = MAX_ITEMS_PER_PAGE;
        } else {
            itemsPerPage = Math.abs(request.getItemsPerPage());
        }

        boolean filterEnabled = false;
        List<String> statusFilter = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getFilter())) {
            if (ACTIVE.equals(request.getFilter())) {
                filterEnabled = true;
                statusFilter.addAll(List.of(DISSOLVED, CONVERTED_CLOSED, REMOVED));
            } else {
                throw new BadRequestException(
                        String.format("Invalid filter parameter supplied: %s, officer ID: %s",
                                request.getFilter(), request.getOfficerId()));
            }
        }

        OfficerAppointmentsAggregate aggregate = repository.findOfficerAppointments(officerId, filterEnabled, statusFilter, startIndex, itemsPerPage);

        MapperRequest mapperRequest = new MapperRequest()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .firstAppointment(firstAppointment.get())
                .aggregate(aggregate);

        if (request.getReturnCounts()) {
            AppointmentCounts appointmentCounts = repository.findOfficerAppointmentCounts(officerId);
            Integer totalCount = aggregate.getTotalResults();
            appointmentCounts.setActiveCount(filterEnabled ? totalCount : totalCount - appointmentCounts.getInactiveCount() - appointmentCounts.getResignedCount());
            return mapper.mapOfficerAppointmentsWithCounts(mapperRequest, appointmentCounts);
        }
        return mapper.mapOfficerAppointments(mapperRequest);
    }
}
