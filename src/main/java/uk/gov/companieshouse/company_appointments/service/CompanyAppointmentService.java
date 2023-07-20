package uk.gov.companieshouse.company_appointments.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.appointment.LinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerList;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.company_appointments.roles.RoleHelper;
import uk.gov.companieshouse.company_appointments.util.CompanyStatusValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class CompanyAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
    private static final String PATCH_APPOINTMENT_ERROR_MESSAGE = "Request failed for company [%s] with appointment [%s], contextId: [%s]: ";
    private static final String PATCH_APPOINTMENTS_ERROR_MESSAGE = "Request failed for company [%s], contextId: [%s]: ";
    private static final int DEFAULT_ITEMS_PER_PAGE = 35;
    private static final int DEFAULT_START_INDEX = 0;

    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final CompanyAppointmentMapper companyAppointmentMapper;
    private final SortMapper sortMapper;
    private final CompanyStatusValidator companyStatusValidator;
    private final CompanyRegisterService companyRegisterService;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository;
    private final Clock clock;

    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper, SortMapper sortMapper,
            CompanyRegisterService companyRegisterService, CompanyMetricsApiService companyMetricsApiService,
            CompanyStatusValidator companyStatusValidator,
            CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository, Clock clock) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
        this.sortMapper = sortMapper;
        this.companyRegisterService = companyRegisterService;
        this.companyMetricsApiService = companyMetricsApiService;
        this.companyStatusValidator = companyStatusValidator;
        this.fullRecordAppointmentRepository = fullRecordAppointmentRepository;
        this.clock = clock;
    }

    public OfficerSummary fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        Optional<CompanyAppointmentData> appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID);
        OfficerSummary officerSummary = companyAppointmentMapper.map(appointmentData.orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber))));
        appointmentData.ifPresent(appt -> {
            officerSummary.etag(appt.getData().getEtag());
            LOGGER.debug(String.format("Found appointment [%s] for company [%s]", appointmentID, companyNumber));
        });
        return officerSummary;
    }

    public OfficerList fetchAppointmentsForCompany(FetchAppointmentsRequest request) throws NotFoundException, BadRequestException, ServiceUnavailableException {
        String companyNumber = request.getCompanyNumber();
        String orderBy = request.getOrderBy();
        LOGGER.debug(String.format("Fetching appointments for company [%s] with order by [%s]", companyNumber, orderBy));

        Boolean registerView = request.getRegisterView();
        if(registerView == null) {
            registerView = false;
        }
        String registerType = request.getRegisterType();
        if (registerView && !companyRegisterService.isRegisterHeldInCompaniesHouse(registerType, new RegistersApi())) {
            throw new NotFoundException("Register not held at Companies House");
        }

        List<CompanyAppointmentData> allAppointmentData;

        Sort sort = sortMapper.getSort(orderBy);

        String filter = request.getFilter();
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

        List<OfficerSummary> officerSummaries = allAppointmentData.stream().map(companyAppointmentMapper::map).collect(Collectors.toList());
        int count = (int) officerSummaries.stream().filter(officer -> officer.getResignedOn() == null).count();
        int activeCount = 0;
        int inactiveCount = 0;
        String appointmentStatus = allAppointmentData.get(0).getCompanyStatus();
        if (appointmentStatus.equals("removed") || appointmentStatus.equals("dissolved") || appointmentStatus.equals("converted-closed")){
            inactiveCount = count;
        } else {
            activeCount = count;
        }

        int resignedCount = (int) officerSummaries.stream().filter(officer -> officer.getResignedOn() != null && officer.getResignedOn().isBefore(ChronoLocalDate.from(LocalDate.now().atStartOfDay()))).count();

        Integer startIndex = request.getStartIndex();
        Integer itemsPerPage = request.getItemsPerPage();
        officerSummaries = addPagingAndStartIndex(officerSummaries, startIndex, itemsPerPage);

        return new OfficerList()
                    .totalResults(officerSummaries.size())
                    .items(officerSummaries)
                    .activeCount(activeCount)
                    .inactiveCount(inactiveCount)
                    .resignedCount(resignedCount)
                    .kind(OfficerList.KindEnum.OFFICER_LIST)
                    .startIndex(startIndex == null ? DEFAULT_START_INDEX : startIndex)
                    .itemsPerPage(itemsPerPage == null ? DEFAULT_ITEMS_PER_PAGE : itemsPerPage)
                    .links(new LinkTypes().self(String.format("/company/%s/officers", companyNumber)))
                    .etag(officerSummaries.isEmpty() ? "" : officerSummaries.get(0).getEtag());
    }

    public void patchCompanyNameStatus(String companyNumber, String companyName,
            String companyStatus)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (isBlank(companyName) || isBlank(companyStatus)) {
            throw new BadRequestException(String.format(PATCH_APPOINTMENTS_ERROR_MESSAGE +
                    "company name and/or company status missing.", companyNumber, getContextId()));
        }
        if (!companyStatusValidator.isValidCompanyStatus(companyStatus)) {
            throw new BadRequestException(String.format(PATCH_APPOINTMENTS_ERROR_MESSAGE +
                    "invalid company status provided.", companyNumber, getContextId()));
        }

        LOGGER.debug(String.format(
                "Patching company name: [%s] and company status [%s] for appointments in company [%s], contextID [%s]",
                companyName, companyStatus, companyNumber, getContextId()));

        try {
            long updatedCount = fullRecordAppointmentRepository.patchAppointmentNameStatusInCompany(
                    companyNumber,
                    companyName, companyStatus, Instant.now(clock),
                    GenerateEtagUtil.generateEtag());
            if (updatedCount == 0) {
                throw new NotFoundException(
                        String.format("No appointments found for company [%s] during PATCH request",
                                companyNumber));
            }
            LOGGER.debug(String.format("Appointments for company [%s] updated successfully",
                    companyNumber));
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException(
                    String.format(PATCH_APPOINTMENTS_ERROR_MESSAGE + "error connecting to MongoDB.",
                            companyNumber, getContextId()));
        }
    }

    public void patchNewAppointmentCompanyNameStatus(String companyNumber, String appointmentId, String companyName,
            String companyStatus) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (isBlank(companyName) || isBlank(companyStatus)) {
            throw new BadRequestException(String.format(PATCH_APPOINTMENT_ERROR_MESSAGE +
                            "company name and/or company status missing.", companyNumber, appointmentId,
                    getContextId()));
        }
        if (!companyStatusValidator.isValidCompanyStatus(companyStatus)) {
            throw new BadRequestException(String.format(PATCH_APPOINTMENT_ERROR_MESSAGE +
                            "invalid company status provided.", companyNumber, appointmentId,
                    getContextId()));
        }

        LOGGER.debug(String.format("Patching company name: [%s] and company status [%s] for company [%s] with appointment [%s]",
                companyName, companyStatus, companyNumber, appointmentId));
        try {
            boolean isUpdated = fullRecordAppointmentRepository.patchAppointmentNameStatus(appointmentId, companyName, companyStatus, Instant.now(clock), GenerateEtagUtil.generateEtag()) == 1L;
            if (!isUpdated) {
                throw new NotFoundException(String.format("Appointment [%s] for company [%s] not found during PATCH request", appointmentId, companyNumber));
            }
            LOGGER.debug(String.format("Appointment [%s] for company [%s] updated successfully", appointmentId, companyNumber));
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException(
                    String.format(PATCH_APPOINTMENT_ERROR_MESSAGE + "error connecting to MongoDB.",
                            companyNumber, appointmentId, getContextId()));
        }
    }

    private List<OfficerSummary> addPagingAndStartIndex(List<OfficerSummary> officerSummaries, Integer startIndex, Integer itemsPerPage) throws NotFoundException {
        int firstItem = 0;
        int lastItem = 35;

        if (startIndex != null) {
            if (startIndex <= officerSummaries.size()){
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

        if (firstItem + lastItem <= officerSummaries.size()) {
            officerSummaries = officerSummaries.subList(firstItem,
                    firstItem + lastItem);
        } else {
            officerSummaries = officerSummaries.subList(firstItem,
                    officerSummaries.size());
        }

        return officerSummaries;
    }

    private String getContextId() {
        return MDC.get(REQUEST_ID.value());
    }
}
