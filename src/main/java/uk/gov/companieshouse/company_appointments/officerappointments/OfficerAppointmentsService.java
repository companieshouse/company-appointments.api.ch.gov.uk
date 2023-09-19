package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;

@Service
class OfficerAppointmentsService {

    private static final int START_INDEX = 0;

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;
    private final FilterService filterService;

    OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper,
            FilterService filterService) {
        this.repository = repository;
        this.mapper = mapper;
        this.filterService = filterService;
    }

    Optional<AppointmentList> getOfficerAppointments(OfficerAppointmentsRequest params) {
        String officerId = params.getOfficerId();
        return repository.findFirstByOfficerId(officerId)
                .flatMap(firstAppointment -> {
                    int startIndex = getStartIndex(params);
                    int itemsPerPage = params.getItemsPerPage();
                    Filter filter = filterService.prepareFilter(params.getFilter(), params.getOfficerId());

                    OfficerAppointmentsAggregate aggregate = repository.findOfficerAppointments(officerId,
                            filter.isFilterEnabled(), filter.getFilterStatuses(), startIndex, itemsPerPage);
                    Set<String> docIds = aggregate.getOfficerAppointments().stream()
                            .map(CompanyAppointmentDocumentId::getId)
                            .collect(Collectors.toSet());
                    return mapper.mapOfficerAppointments(new MapperRequest()
                            .startIndex(startIndex)
                            .itemsPerPage(itemsPerPage)
                            .firstAppointment(firstAppointment)
                            .aggregate(aggregate)
                            .officerAppointments(repository.findByIdIn(docIds))
                    );
                });
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
