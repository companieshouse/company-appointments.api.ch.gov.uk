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
        boolean filter = "active".equals(request.getFilter());
        int startIndex = request.getStartIndex() == null ? START_INDEX : request.getStartIndex();
        int itemsPerPage = request.getItemsPerPage() == null ? ITEMS_PER_PAGE : request.getItemsPerPage();

        return mapper.mapOfficerAppointments(startIndex, itemsPerPage,
                repository.findOfficerAppointments(request.getOfficerId(), filter, startIndex, itemsPerPage));
    }
}
