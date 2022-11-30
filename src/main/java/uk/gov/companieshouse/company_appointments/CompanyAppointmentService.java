package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    private CompanyAppointmentRepository companyAppointmentRepository;
    private CompanyAppointmentMapper companyAppointmentMapper;

    @Autowired
    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public CompanyAppointmentView fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentData> appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentData.orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber))));
    }

    public AllCompanyAppointmentsView fetchAppointmentsForCompany(String companyNumber, String filter) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointments for company [%s]", companyNumber));
        List<CompanyAppointmentData> allAppointmentData;

        if(filter != null && filter.equals("active")){
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumberForNotResigned(companyNumber);
        } else {
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumber(companyNumber);
        }

        if (allAppointmentData.isEmpty()) {
            throw new NotFoundException(String.format("Appointments for company [%s] not found", companyNumber));
        }

        List<CompanyAppointmentView> companyAppointmentViews = allAppointmentData.stream().map(companyAppointmentMapper :: map ).collect(Collectors.toList());
        companyAppointmentViews.sort(new CompanyAppointmentComparator());
        int activeCount = (int)companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() == null).count();
        int inactiveCount = 0;
        int resignedCount = (int)companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() !=null && officer.getResignedOn().isBefore(LocalDate.now().atStartOfDay())).count();

        return new AllCompanyAppointmentsView(companyAppointmentViews.size(), companyAppointmentViews, activeCount, inactiveCount, resignedCount);

    }
}
