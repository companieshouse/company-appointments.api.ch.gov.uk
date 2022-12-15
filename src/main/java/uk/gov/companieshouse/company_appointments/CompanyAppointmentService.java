package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.LocalDate;
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

    public AllCompanyAppointmentsView fetchAppointmentsForCompany(String companyNumber, String filter, String orderBy) throws NotFoundException, BadRequestException {
        LOGGER.debug(String.format("Fetching appointments for company [%s]", companyNumber));
        List<CompanyAppointmentData> allAppointmentData;
        Sort sort = getSorting(orderBy);
                
        if(filter != null && filter.equals("active")){
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumberForNotResigned(companyNumber, sort);
        } else {
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumber(companyNumber, sort);
        }

        if (allAppointmentData.isEmpty()) {
            throw new NotFoundException(String.format("Appointments for company [%s] not found", companyNumber));
        }

        List<CompanyAppointmentView> companyAppointmentViews = allAppointmentData.stream().map(companyAppointmentMapper :: map ).collect(Collectors.toList());
        int activeCount = (int)companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() == null).count();

        int resignedCount = (int)companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() !=null && officer.getResignedOn().isBefore(LocalDate.now().atStartOfDay())).count();

        return new AllCompanyAppointmentsView(companyAppointmentViews.size(), companyAppointmentViews, activeCount, 0, resignedCount);

    }

    private Sort getSorting(String orderBy) throws BadRequestException{
        if(orderBy == null) {
            return Sort.by(Sort.Direction.ASC, "officer_role_sort_order")
            .and(Sort.by(Sort.Direction.ASC, "data.surname", "data.company_name"))
            .and(Sort.by(Sort.Direction.ASC, "data.forename"))
            .and(Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before"));
        } else if(orderBy.equals("appointed_on")) {
            return Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before");
        } else if(orderBy.equals("surname")) {
            return Sort.by(Sort.Direction.ASC, "data.surname", "data.company_name");
        } else if(orderBy.equals("resigned_on")) {
            return Sort.by(Sort.Direction.DESC, "data.resigned_on");
        } else {
            throw new BadRequestException(String.format("Invalid order by parameter [%s]", orderBy));
        }
    }
}
