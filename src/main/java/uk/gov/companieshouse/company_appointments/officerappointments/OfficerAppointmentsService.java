package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;

@Service
class OfficerAppointmentsService {

    private static final int ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE = 50;
    private static final int START_INDEX = 0;

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;
    private final FilterService filterService;
    private final AppointmentsCountsService appointmentsCountsService;

    OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper,
            FilterService filterService, AppointmentsCountsService appointmentsCountsService) {
        this.repository = repository;
        this.mapper = mapper;
        this.filterService = filterService;
        this.appointmentsCountsService = appointmentsCountsService;
    }

    Optional<AppointmentList> getOfficerAppointments(
            OfficerAppointmentsRequest request) throws BadRequestException {
        String officerId = request.getOfficerId();

        Optional<CompanyAppointmentData> firstAppointment = repository.findFirstByOfficerId(officerId);
        if (firstAppointment.isEmpty()) {
            return Optional.empty();
        }

        int startIndex = getStartIndex(request);
        int itemsPerPage = getItemsPerPage(request);
        Filter filter = filterService.prepareFilter(request.getFilter(), request.getOfficerId());

        OfficerAppointmentsAggregate aggregate = repository.findOfficerAppointments(officerId, filter.isFilterEnabled(),
                filter.getFilterStatuses(), startIndex, itemsPerPage);

        MapperRequest mapperRequest = new MapperRequest()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .firstAppointment(firstAppointment.get())
                .aggregate(aggregate);

        if (request.getReturnCounts()) {
            return mapper.mapOfficerAppointmentsWithCounts(mapperRequest,
                    appointmentsCountsService.getAppointmentsCounts(officerId,
                            filter.isFilterEnabled(),
                            aggregate.getTotalResults()));
        } else {
            return mapper.mapOfficerAppointments(mapperRequest);
        }
    }

    private static int getItemsPerPage(OfficerAppointmentsRequest request) {
        int itemsPerPage;
        if (request.getItemsPerPage() == null || request.getItemsPerPage() == 0) {
            itemsPerPage = ITEMS_PER_PAGE;
        } else if (Math.abs(request.getItemsPerPage()) > 50) {
            itemsPerPage = MAX_ITEMS_PER_PAGE;
        } else {
            itemsPerPage = Math.abs(request.getItemsPerPage());
        }
        return itemsPerPage;
    }

    private static int getStartIndex(OfficerAppointmentsRequest request) {
        int startIndex;
        if (request.getStartIndex() == null) {
            startIndex = START_INDEX;
        } else {
            startIndex = Math.abs(request.getStartIndex());
        }
        return startIndex;
    }
}
