package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Service
public class OfficerAppointmentsService {

    private static final int ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE = 50;
    private static final int START_INDEX = 0;

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;

    public OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    protected Optional<AppointmentList> getOfficerAppointments(OfficerAppointmentsRequest request) {
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

        boolean filter = "active".equals(request.getFilter());
        return mapper.mapOfficerAppointments(startIndex, itemsPerPage,
                firstAppointment.get(), repository.findOfficerAppointments(officerId, filter, startIndex, itemsPerPage));
    }
}
