package uk.gov.companieshouse.company_appointments.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import java.time.Clock;
import java.time.Instant;
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
import uk.gov.companieshouse.api.metrics.AppointmentsApi;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
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

    public OfficerList fetchAppointmentsForCompany(FetchAppointmentsRequest request) throws NotFoundException, ServiceUnavailableException {
        String companyNumber = request.getCompanyNumber();
        String orderBy = request.getOrderBy();
        LOGGER.debug(
                String.format("Fetching appointments for company [%s] with order by [%s]", companyNumber, orderBy));

        final boolean registerView = request.getRegisterView() != null && request.getRegisterView();

        MetricsApi metricsApi = companyMetricsApiService.invokeGetMetricsApi(companyNumber).getData();

        String registerType = request.getRegisterType();
        if (registerView && !companyRegisterService.isRegisterHeldInCompaniesHouse(registerType,
                metricsApi.getRegisters())) {
            throw new NotFoundException("Register not held at Companies House");
        }

        Sort sort = sortMapper.getSort(orderBy);

        String filter = request.getFilter();

        List<CompanyAppointmentData> allAppointmentData;
        boolean isFilterEnabled = false;
        if ((filter != null && filter.equals("active")) || registerView) {
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumberForNotResigned(companyNumber, sort);
            isFilterEnabled = true;
        } else {
            allAppointmentData = companyAppointmentRepository.readAllByCompanyNumber(companyNumber, sort);
        }
        if (allAppointmentData.isEmpty()) {
            throw new NotFoundException(String.format("Appointments for company [%s] not found", companyNumber));
        }
        companyAppointmentMapper.setRegisterView(registerView);
        AppointmentsApi appointmentsCounts = Optional.ofNullable(metricsApi.getCounts())
                .map(CountsApi::getAppointments)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointments metrics for company number [%s] not found", companyNumber)));
        Counts counts;
        if (registerView) {
            allAppointmentData = allAppointmentData.stream().filter(d -> RoleHelper.isRegisterType(d, registerType))
                    .collect(Collectors.toList());
            counts = new Counts(appointmentsCounts, registerType);
        } else {
            counts = new Counts(appointmentsCounts, allAppointmentData.get(0).getCompanyStatus(), isFilterEnabled);
        }

        List<OfficerSummary> officerSummaries = allAppointmentData.stream().map(companyAppointmentMapper::map)
                .collect(Collectors.toList());


        Integer startIndex = request.getStartIndex();
        Integer itemsPerPage = request.getItemsPerPage();
        officerSummaries = addPagingAndStartIndex(officerSummaries, startIndex, itemsPerPage);

        return new OfficerList()
                    .totalResults(counts.getTotalResults())
                    .items(officerSummaries)
                    .activeCount(counts.getActive())
                    .inactiveCount(counts.getInactive())
                    .resignedCount(counts.getResigned())
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

    private static class Counts {

        private final int totalResults;
        private final int active;
        private final Integer inactive;
        private final int resigned;

        private Counts(final AppointmentsApi appointments, final String registerType) {
            switch (registerType) {
                case "directors":
                    this.active = appointments.getActiveDirectorsCount();
                    this.resigned = 0;
                    totalResults = appointments.getActiveDirectorsCount();
                    break;
                case "secretaries":
                    active = appointments.getActiveSecretariesCount();
                    resigned = 0;
                    totalResults = appointments.getActiveSecretariesCount();
                    break;
                case "llp_members":
                    active = appointments.getActiveLlpMembersCount();
                    resigned = 0;
                    totalResults = appointments.getActiveLlpMembersCount();
                    break;
                default:
                    active = appointments.getActiveCount();
                    resigned = appointments.getResignedCount();
                    totalResults = appointments.getTotalCount();
            }
            inactive = null;
        }

        private Counts(final AppointmentsApi appointments, final String status, final boolean isFilterEnabled) {
            switch (status) {
                case "removed":
                case "dissolved":
                case "converted-closed":
                    active = 0;
                    inactive = appointments.getActiveCount();
                    if(isFilterEnabled) {
                        totalResults = 0;
                    } else {
                        totalResults = appointments.getTotalCount();
                    }
                    break;
                default:
                    active = appointments.getActiveCount();
                    inactive = 0;
                    if(isFilterEnabled){
                        totalResults = appointments.getActiveCount();
                    } else {
                        totalResults = appointments.getTotalCount();
                    }
            }
            resigned = appointments.getResignedCount();
        }

        public int getTotalResults() {
            return totalResults;
        }

        public int getActive() {
            return active;
        }

        public Integer getInactive() {
            return inactive;
        }

        public int getResigned() {
            return resigned;
        }
    }
}
