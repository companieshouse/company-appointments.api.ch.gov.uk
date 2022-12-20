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
    private SortMapper sortMapper;

    @Autowired
    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper, SortMapper sortMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
        this.sortMapper = sortMapper;
    }

    public CompanyAppointmentView fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentData> appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentData.orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber))));
    }

    public AllCompanyAppointmentsView fetchAppointmentsForCompany(String companyNumber, String filter, String orderBy, Integer startIndex, Integer itemsPerPage) throws NotFoundException, BadRequestException {
        LOGGER.debug(String.format("Fetching appointments for company [%s] with order by [%s]", companyNumber, orderBy));
        List<CompanyAppointmentData> allAppointmentData;

        Sort sort = sortMapper.getSort(orderBy);
                
        if (filter != null && filter.equals("active")){
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumberForNotResigned(companyNumber, sort);
        } else {
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumber(companyNumber, sort);
        }

        if (allAppointmentData.isEmpty()) {
            throw new NotFoundException(String.format("Appointments for company [%s] not found", companyNumber));
        }

        List<CompanyAppointmentView> companyAppointmentViews = allAppointmentData.stream().map(companyAppointmentMapper::map).collect(Collectors.toList());
        int activeCount = (int) companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() == null).count();

        int resignedCount = (int) companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() != null && officer.getResignedOn().isBefore(LocalDate.now().atStartOfDay())).count();

        companyAppointmentViews = addPagingAndStartIndex(companyAppointmentViews, startIndex, itemsPerPage);

        return new AllCompanyAppointmentsView(companyAppointmentViews.size(), companyAppointmentViews, activeCount, 0, resignedCount);
    }

    public List<CompanyAppointmentView> addPagingAndStartIndex(List<CompanyAppointmentView> companyAppointmentViews, Integer startIndex, Integer itemsPerPage) throws NotFoundException {
        int firstItem = 0;
        int lastItem = 35;

        if (startIndex != null) {
            if (startIndex <= companyAppointmentViews.size()){
                firstItem = startIndex;
            } else {
                throw new NotFoundException("Index too high");
            }
        }

        if (itemsPerPage != null) {
            if (itemsPerPage <= 100) {
                lastItem = itemsPerPage;
            } else {
                lastItem = 100;
            }
        }

        if (firstItem + lastItem <= companyAppointmentViews.size()) {
            companyAppointmentViews = companyAppointmentViews.subList(firstItem, firstItem + lastItem);
        } else {
            companyAppointmentViews = companyAppointmentViews.subList(firstItem, companyAppointmentViews.size());
        }

        return companyAppointmentViews;
    }
}
