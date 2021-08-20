package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.model.delta.officers.OfficersApi;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDeltaData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Optional;

@Service
public class CompanyAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private CompanyAppointmentRepository companyAppointmentRepository;
    private CompanyAppointmentDeltaRepository companyAppointmentDeltaRepository;
    private CompanyAppointmentMapper companyAppointmentMapper;

    @Autowired
    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
                                     CompanyAppointmentDeltaRepository companyAppointmentDeltaRepository,
                                     CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentDeltaRepository = companyAppointmentDeltaRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public CompanyAppointmentView fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentData> appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentData.orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber))));
    }

    public void putOfficerData(final OfficersApi companyAppointmentData) {
        companyAppointmentDeltaRepository.insert(companyAppointmentData);
    }

}
