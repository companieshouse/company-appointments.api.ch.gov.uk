package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;

@Service
class OfficerAppointmentsService {

    private static final int DEFAULT_START_INDEX = 0;

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
        String officerId = params.officerId();
        return repository.findFirstByOfficerId(officerId)
                .flatMap(firstAppointment -> {
                    int startIndex = getStartIndex(params.startIndex());
                    int itemsPerPage = params.itemsPerPage();

                    Filter filter = filterService.prepareFilter(params.filter(), params.officerId());
                    boolean filterEnabled = filter.isFilterEnabled();
                    List<String> filterStatuses = filter.filterStatuses();

                    OfficerAppointments officerAppointments = repository.findOfficerAppointmentsIds(officerId,
                            filterEnabled, filterStatuses, startIndex, itemsPerPage);
                    List<CompanyAppointmentDocument> documents = repository.findFullOfficerAppointments(
                            officerAppointments.getIds());

                    final int totalResults = repository.countTotal(officerId, filterEnabled, filterStatuses);
                    final int resignedCount = filterEnabled ? 0 : repository.countResigned(officerId);
                    final int inactiveCount = filterEnabled ? 0 : repository.countInactive(officerId);

                    return mapper.mapOfficerAppointments(MapperRequest.builder()
                            .startIndex(startIndex)
                            .itemsPerPage(itemsPerPage)
                            .firstAppointment(firstAppointment)
                            .officerAppointments(documents)
                            .totalResults(totalResults)
                            .resignedCount(resignedCount)
                            .inactiveCount(inactiveCount)
                            .build());
                });
    }

    private static int getStartIndex(Integer requestStartIndex) {
        int startIndex;
        if (requestStartIndex == null) {
            startIndex = DEFAULT_START_INDEX;
        } else {
            startIndex = Math.abs(requestStartIndex);
        }
        return startIndex;
    }
}
