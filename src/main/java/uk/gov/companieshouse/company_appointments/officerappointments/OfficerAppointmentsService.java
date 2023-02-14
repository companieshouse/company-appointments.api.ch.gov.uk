package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.officerappointments.OfficerAppointmentsApi;

@Service
public class OfficerAppointmentsService {

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;

    public OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<OfficerAppointmentsApi> getOfficerAppointments(OfficerAppointmentsRequest request) {
        return repository.findOfficerAppointments(request.getOfficerId())
                .flatMap(aggregate -> mapper.map(aggregate, request));
    }
}
