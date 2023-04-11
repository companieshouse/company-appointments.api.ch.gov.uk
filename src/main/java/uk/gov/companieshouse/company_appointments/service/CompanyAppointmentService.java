package uk.gov.companieshouse.company_appointments.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.model.view.AllCompanyAppointmentsView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.company_appointments.roles.RoleHelper;
import uk.gov.companieshouse.company_appointments.util.CompanyStatusValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class CompanyAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
    private static final String ERROR_MESSAGE = "Request failed for company [%s] with appointment [%s]: ";

    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final CompanyAppointmentMapper companyAppointmentMapper;
    private final SortMapper sortMapper;
    private final CompanyStatusValidator companyStatusValidator;
    private final CompanyRegisterService companyRegisterService;
    private final CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository;
    private final ResourceChangedApiService resourceChangedApiService;
    private final Clock clock;

    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper, SortMapper sortMapper,
            CompanyRegisterService companyRegisterService, CompanyStatusValidator companyStatusValidator,
            CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository,
            ResourceChangedApiService resourceChangedApiService, Clock clock) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
        this.sortMapper = sortMapper;
        this.companyRegisterService = companyRegisterService;
        this.companyStatusValidator = companyStatusValidator;
        this.fullRecordAppointmentRepository = fullRecordAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.clock = clock;
    }

    public CompanyAppointmentView fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentData> appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        appointmentData.ifPresent(appt -> LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentData.orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber))));
    }

    public AllCompanyAppointmentsView fetchAppointmentsForCompany(String companyNumber, String filter, String orderBy, Integer startIndex, Integer itemsPerPage,
            Boolean registerView, String registerType) throws NotFoundException, BadRequestException, ServiceUnavailableException {
        LOGGER.debug(String.format("Fetching appointments for company [%s] with order by [%s]", companyNumber, orderBy));

        if(registerView == null) {
            registerView = false;
        }
        if (registerView && !companyRegisterService.isRegisterHeldInCompaniesHouse(registerType, companyNumber)) {
            throw new NotFoundException("Register not held at Companies House");
        }

        List<CompanyAppointmentData> allAppointmentData;

        Sort sort = sortMapper.getSort(orderBy);
                
        if ((filter != null && filter.equals("active")) || registerView){
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumberForNotResigned(companyNumber, sort);
        } else {
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumber(companyNumber, sort);
        }
        companyAppointmentMapper.setRegisterView(registerView);
        if(registerView) {
            allAppointmentData = allAppointmentData.stream().filter(d -> RoleHelper.isRegisterType(d, registerType)).collect(Collectors.toList());
        }

        if (allAppointmentData.isEmpty()) {
            throw new NotFoundException(String.format("Appointments for company [%s] not found", companyNumber));
        }

        List<CompanyAppointmentView> companyAppointmentViews = allAppointmentData.stream().map(companyAppointmentMapper::map).collect(Collectors.toList());
        int count = (int) companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() == null).count();
        int activeCount = 0;
        int inactiveCount = 0;
        String appointmentStatus = allAppointmentData.get(0).getCompanyStatus();
        if (appointmentStatus.equals("removed") || appointmentStatus.equals("dissolved") || appointmentStatus.equals("converted-closed")){
            inactiveCount = count;
        } else {
            activeCount = count;
        }

        int resignedCount = (int) companyAppointmentViews.stream().filter(officer -> officer.getResignedOn() != null && officer.getResignedOn().isBefore(LocalDate.now().atStartOfDay())).count();

        companyAppointmentViews = addPagingAndStartIndex(companyAppointmentViews, startIndex, itemsPerPage);

        return new AllCompanyAppointmentsView(companyAppointmentViews.size(), companyAppointmentViews, activeCount, inactiveCount, resignedCount);
    }

    public void patchCompanyNameStatus(String companyNumber, String companyName,
            String companyStatus) throws NotFoundException {
        // Implementation started as part of pair programming session, to be implemented in DSND-1531
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void patchNewAppointmentCompanyNameStatus(String companyNumber, String appointmentId, String companyName,
            String companyStatus, String contextId) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (isBlank(companyName) || isBlank(companyStatus)) {
            throw new BadRequestException(String.format(ERROR_MESSAGE + "company name and/or company status missing.", companyNumber, appointmentId));
        }
        if (!companyStatusValidator.isValidCompanyStatus(companyStatus)) {
            throw new BadRequestException(String.format(ERROR_MESSAGE + "invalid company status provided.", companyNumber, appointmentId));
        }

        LOGGER.debug(String.format("Patching company name: [%s] and company status [%s] for company [%s] with appointment [%s]",
                companyName, companyStatus, companyNumber, appointmentId));
        try {
            if (!fullRecordAppointmentRepository.existsById(appointmentId)) {
                throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentId, companyNumber));
            }
            resourceChangedApiService.invokeChsKafkaApi(new ResourceChangedRequest(contextId, companyNumber, appointmentId, null, false));
            LOGGER.debug(String.format("ChsKafka api CHANGED invoked updated successfully for context id: %s and company number: %s",
                    contextId,
                    companyNumber));

            boolean isUpdated = fullRecordAppointmentRepository.patchAppointmentNameStatus(appointmentId, companyName, companyStatus, Instant.now(clock), GenerateEtagUtil.generateEtag()) == 1L;
            if (!isUpdated) {
                throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found during PATCH request", appointmentId, companyNumber));
            }
            LOGGER.debug(String.format("Appointment [%s] for company [%s] updated successfully", appointmentId, companyNumber));
        } catch (IllegalArgumentException ex) {
            throw new ServiceUnavailableException(String.format(ERROR_MESSAGE + "error calling CHS Kafka API.", companyNumber, appointmentId));
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException(String.format(ERROR_MESSAGE + "error connecting to MongoDB.", companyNumber, appointmentId));
        }
    }

    private List<CompanyAppointmentView> addPagingAndStartIndex(List<CompanyAppointmentView> companyAppointmentViews, Integer startIndex, Integer itemsPerPage) throws NotFoundException {
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
