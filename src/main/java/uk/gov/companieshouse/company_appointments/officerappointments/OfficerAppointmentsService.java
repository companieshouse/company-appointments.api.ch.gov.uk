package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;

@Service
class OfficerAppointmentsService {

    private static final int DEFAULT_START_INDEX = 0;

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;
    private final FilterService filterService;
    private final ItemsPerPageService itemsPerPageService;
    private final SortingThresholdService sortingThresholdService;

    OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper,
            FilterService filterService, ItemsPerPageService itemsPerPageService,
            SortingThresholdService sortingThresholdService) {
        this.repository = repository;
        this.mapper = mapper;
        this.filterService = filterService;
        this.itemsPerPageService = itemsPerPageService;
        this.sortingThresholdService = sortingThresholdService;
    }

    Optional<AppointmentList> getOfficerAppointments(OfficerAppointmentsRequest params) {
        final String officerId = params.officerId();
        final String authPrivileges = params.authPrivileges();
        final int startIndex = getStartIndex(params.startIndex());
        final int adjustedItemsPerPage = itemsPerPageService.adjustItemsPerPage(params.itemsPerPage(), authPrivileges);

        Filter filter = filterService.prepareFilter(params.filter(), params.officerId());
        boolean filterEnabled = filter.isFilterEnabled();
        List<String> filterStatuses = filter.filterStatuses();

        final int totalResults = repository.countTotal(officerId, filterEnabled, filterStatuses);

        List<CompanyAppointmentDocument> documents;

        if (sortingThresholdService.shouldSortByActiveThenResigned(totalResults, authPrivileges)) {
            List<String> appointmentsIds = repository.findOfficerAppointmentsIds(officerId, filterEnabled, filterStatuses,
                    startIndex, adjustedItemsPerPage).getIds();

            if (!appointmentsIds.isEmpty()) {
                documents = repository.findFullOfficerAppointments(appointmentsIds);
            } else {
                documents = List.of();
            }
        } else {
            documents = repository.findRecentOfficerAppointments(officerId, filterEnabled, filterStatuses, startIndex,
                    adjustedItemsPerPage);
        }

        final int resignedCount = filterEnabled ? 0 : repository.countResigned(officerId);
        final int inactiveCount = filterEnabled ? 0 : repository.countInactive(officerId);

        CompanyAppointmentDocument firstAppointment = filterService.findFirstActiveAppointment(documents)
                .orElseGet(() -> repository.findLatestAppointment(officerId));

        return mapper.mapOfficerAppointments(MapperRequest.builder()
                .startIndex(startIndex)
                .itemsPerPage(adjustedItemsPerPage)
                .firstAppointment(firstAppointment)
                .officerAppointments(documents)
                .totalResults(totalResults)
                .resignedCount(resignedCount)
                .inactiveCount(inactiveCount)
                .build());
    }

    private static int getStartIndex(Integer requestStartIndex) {
        int startIndex;
        if (requestStartIndex == null) {
            startIndex = DEFAULT_START_INDEX;
        } else {
            startIndex = Math.abs(requestStartIndex);
        }
        DataMapHolder.get().startIndex(String.valueOf(startIndex));
        return startIndex;
    }
}
