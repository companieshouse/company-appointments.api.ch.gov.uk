package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;

@Service
public class OfficerAppointmentsService {

    private static final int ITEMS_PER_PAGE = 35;
    private static final int START_INDEX = 0;

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;

    public OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    protected Optional<AppointmentList> getOfficerAppointments(OfficerAppointmentsRequest request) {
        int startIndex;
        if (request.getStartIndex() == null) {
            startIndex = START_INDEX;
        } else {
            startIndex = Math.abs(request.getStartIndex());
        }

        int itemsPerPage;
        if (request.getItemsPerPage() == null || request.getItemsPerPage() == 0) {
            itemsPerPage = ITEMS_PER_PAGE;
        } else {
            itemsPerPage = Math.abs(request.getItemsPerPage());
        }

        boolean filter = "active".equals(request.getFilter());
        return mapper.mapOfficerAppointments(startIndex, itemsPerPage,
                repository.findOfficerAppointments(request.getOfficerId(), filter, startIndex, itemsPerPage));
    }
}
