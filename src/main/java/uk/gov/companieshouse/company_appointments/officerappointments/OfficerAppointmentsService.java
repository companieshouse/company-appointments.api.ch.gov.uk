package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.officer.AppointmentList;

@Service
public class OfficerAppointmentsService {

    private final OfficerAppointmentsRepository repository;
    private final OfficerAppointmentsMapper mapper;

    public OfficerAppointmentsService(OfficerAppointmentsRepository repository, OfficerAppointmentsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<AppointmentList> getOfficerAppointments(OfficerAppointmentsRequest request) {
        return mapper.mapOfficerAppointments(repository.findOfficerAppointments(request.getOfficerId()));
    }
}