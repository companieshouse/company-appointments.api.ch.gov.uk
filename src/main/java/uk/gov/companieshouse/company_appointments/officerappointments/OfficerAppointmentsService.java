package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsMapper.MapperRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
class OfficerAppointmentsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);

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
        String officerId = params.officerId();
        return repository.findFirstByOfficerId(officerId)
                .flatMap(firstAppointment -> {
                    int startIndex = getStartIndex(params);
                    int itemsPerPage = params.itemsPerPage();
                    Filter filter = filterService.prepareFilter(params.filter(),
                            params.officerId());

                    OfficerAppointmentsAggregate aggregate;
                    try {
                        aggregate = repository.findOfficerAppointments(officerId,
                                filter.isFilterEnabled(), filter.getFilterStatuses(), startIndex, itemsPerPage);
                    } catch (UncategorizedMongoDbException e) {
                        LOGGER.debug(String.format("Retrying findOfficerAppointments due to "
                                        + "exceeding Mongo query resource limits. Cause: %s",
                                e.getCause().getMessage()), DataMapHolder.getLogMap());

                        aggregate = findOfficerWithLargeAppointmentsCount(officerId, filter, startIndex,
                                itemsPerPage);
                    }

                    return mapper.mapOfficerAppointments(new MapperRequest()
                            .startIndex(startIndex)
                            .itemsPerPage(itemsPerPage)
                            .firstAppointment(firstAppointment)
                            .aggregate(aggregate));
                });
    }

    public OfficerAppointmentsAggregate findOfficerWithLargeAppointmentsCount(String officerId,
            Filter filter, int startIndex, int itemsPerPage) {
        OfficerAppointmentsAggregate sparseAggregate = repository.findOfficerAppointmentsSparseAggregate(
                officerId, filter.isFilterEnabled(), filter.getFilterStatuses(), startIndex, itemsPerPage);

        List<String> docIds = sparseAggregate.getOfficerAppointments().stream()
                .map(CompanyAppointmentDocument::getId)
                .collect(Collectors.toList());

        List<CompanyAppointmentDocument> documents = repository.findOfficerAppointmentsInIdList(docIds,
                filter.isFilterEnabled(), filter.getFilterStatuses());

        sparseAggregate.officerAppointments(documents);

        return sparseAggregate;
    }

    public OfficerAppointmentsAggregate findOfficerOfficerRoleSortOrder(String officerId,
            Filter filter, int startIndex, int itemsPerPage) {

        OfficerAppointmentsAggregate sparseAggregate = repository.findOfficerAppointmentsOfficerRoleSortOrder(
                officerId, filter.isFilterEnabled(), filter.getFilterStatuses(), startIndex, itemsPerPage);

        List<String> docIds = sparseAggregate.getOfficerAppointments().stream()
                .map(CompanyAppointmentDocument::getId)
                .collect(Collectors.toList());

        List<CompanyAppointmentDocument> documents = repository.findOfficerAppointmentsInIdList(docIds,
                filter.isFilterEnabled(), filter.getFilterStatuses());

        return sparseAggregate.officerAppointments(documents);
    }

    public OfficerAppointmentsAggregate findOfficerSeparateCalls(String officerId,
            Filter filter, int startIndex, int itemsPerPage) {
        return new OfficerAppointmentsAggregate()
                .officerAppointments(
                        repository.findOnlyOfficerAppointmentsOfficerRoleSortOrder(officerId, filter.isFilterEnabled(),
                                filter.getFilterStatuses(), startIndex, itemsPerPage))
                .totalResults(repository.countTotal(officerId, filter.isFilterEnabled(), filter.getFilterStatuses()))
                .resignedCount(repository.countResigned(officerId))
                .inactiveCount(repository.countInactive(officerId));
    }

    /*
    This method is a specific optimisation after identifying an unacceptable performance degradation when an officer
    has several thousand appointments. The multiple round trips to the database is by design.
     */
    public OfficerAppointmentsAggregate findOfficerCorrectSortingSeparateCalls(String officerId,
            Filter filter, int startIndex, int itemsPerPage) {

        OfficerAppointmentsAggregate sparseAggregate = repository.findOfficerAppointmentsNoCounts(
                officerId, filter.isFilterEnabled(), filter.getFilterStatuses(), startIndex, itemsPerPage);

        List<String> docIds = sparseAggregate.getOfficerAppointments().stream()
                .map(CompanyAppointmentDocument::getId)
                .collect(Collectors.toList());

        List<CompanyAppointmentDocument> documents = repository.findOfficerAppointmentsInIdList(docIds,
                filter.isFilterEnabled(), filter.getFilterStatuses());

        return sparseAggregate
                .officerAppointments(documents)
                .totalResults(repository.countTotal(officerId, filter.isFilterEnabled(), filter.getFilterStatuses()))
                .resignedCount(repository.countResigned(officerId))
                .inactiveCount(repository.countInactive(officerId));
    }

    private static int getStartIndex(OfficerAppointmentsRequest request) {
        int startIndex;
        if (request.startIndex() == null) {
            startIndex = START_INDEX;
        } else {
            startIndex = Math.abs(request.startIndex());
        }
        return startIndex;
    }
}
