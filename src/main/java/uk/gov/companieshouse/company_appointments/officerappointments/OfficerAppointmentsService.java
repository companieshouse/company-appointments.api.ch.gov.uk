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

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

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
                    Filter filter = filterService.prepareFilter(params.getFilter(),
                            params.getOfficerId());

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

    private OfficerAppointmentsAggregate findOfficerWithLargeAppointmentsCount(String officerId,
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
