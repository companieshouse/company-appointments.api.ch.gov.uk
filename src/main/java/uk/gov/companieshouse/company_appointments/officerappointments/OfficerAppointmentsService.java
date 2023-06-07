package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
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

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;
    private final ServiceFilter serviceFilter;

    public OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper,
            ServiceFilter serviceFilter) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceFilter = serviceFilter;
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

        Filter filter = serviceFilter.prepareFilter(request.getFilter(), request.getOfficerId());

        OfficerAppointmentsAggregate aggregate = repository.findOfficerAppointments(officerId, filter.isFilterEnabled(),
                filter.getFilterStatuses(), startIndex, itemsPerPage);

        MapperRequest mapperRequest = new MapperRequest()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .firstAppointment(firstAppointment.get())
                .aggregate(aggregate);

        if (request.getReturnCounts()) {
            AppointmentCounts appointmentCounts = repository.findOfficerAppointmentCounts(officerId);
            appointmentCounts.totalCount(aggregate.getTotalResults());
            appointmentCounts.activeCount(filter.getActiveCountFormula().applyAsInt(appointmentCounts));
            return mapper.mapOfficerAppointmentsWithCounts(mapperRequest, appointmentCounts);
        }
        return mapper.mapOfficerAppointments(mapperRequest);
    }
}
