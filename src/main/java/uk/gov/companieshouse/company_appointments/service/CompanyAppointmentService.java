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
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentFullRecordRepository;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
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
    private static final String ACTIVE = "active";

    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final CompanyAppointmentMapper companyAppointmentMapper;
    private final CompanyStatusValidator companyStatusValidator;
    private final CompanyRegisterService companyRegisterService;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository;
    private final Clock clock;

    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper,
            CompanyRegisterService companyRegisterService,
            CompanyMetricsApiService companyMetricsApiService,
            CompanyStatusValidator companyStatusValidator,
            CompanyAppointmentFullRecordRepository fullRecordAppointmentRepository,
            Clock clock) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
        this.companyRegisterService = companyRegisterService;
        this.companyMetricsApiService = companyMetricsApiService;
        this.companyStatusValidator = companyStatusValidator;
        this.fullRecordAppointmentRepository = fullRecordAppointmentRepository;
        this.clock = clock;
    }

    public OfficerSummary fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber));
        CompanyAppointmentData appointmentData = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID)
                .orElseThrow(() -> new NotFoundException(String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentData)
                .etag(appointmentData.getData().getEtag());
    }

    public OfficerList fetchAppointmentsForCompany(FetchAppointmentsRequest request) throws NotFoundException, ServiceUnavailableException {
        String companyNumber = request.getCompanyNumber();
        String orderBy = request.getOrderBy();
        String filter = request.getFilter();
        String registerType = request.getRegisterType();
        int startIndex = Optional.ofNullable(request.getStartIndex())
                .orElse(DEFAULT_START_INDEX);
        int itemsPerPage = Optional.ofNullable(request.getItemsPerPage())
                .orElse(DEFAULT_ITEMS_PER_PAGE);
        final boolean registerView = request.getRegisterView() != null && request.getRegisterView();
        boolean filterActiveOnly = (filter != null && filter.equals(ACTIVE)) || registerView;

        LOGGER.debug(
                String.format("Fetching appointments for company [%s] with order by [%s]", companyNumber, orderBy));

        MetricsApi metricsApi = companyMetricsApiService.invokeGetMetricsApi(companyNumber).getData();

        if (registerView && !companyRegisterService.isRegisterHeldInCompaniesHouse(registerType,
                metricsApi.getRegisters())) {
            throw new NotFoundException("Register not held at Companies House");
        }

        List<CompanyAppointmentData> allAppointmentData = companyAppointmentRepository.getCompanyAppointmentData(
                companyNumber, orderBy, registerType, startIndex, itemsPerPage, registerView,
                filterActiveOnly);

        if (allAppointmentData.isEmpty()) {
            throw new NotFoundException(String.format("Appointments for company [%s] not found", companyNumber));
        }
        AppointmentsApi appointmentsCounts = Optional.ofNullable(metricsApi.getCounts())
                .map(CountsApi::getAppointments)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointments metrics for company number [%s] not found", companyNumber)));

        Counts counts = registerView? new Counts(appointmentsCounts, registerType) :
                new Counts(appointmentsCounts, allAppointmentData.get(0).getCompanyStatus(), filterActiveOnly);

        List<OfficerSummary> officerSummaries = allAppointmentData.stream()
                .map(appointment -> companyAppointmentMapper.map(appointment, registerView))
                .collect(Collectors.toList());

        return new OfficerList()
                    .totalResults(counts.getTotalResults())
                    .items(officerSummaries)
                    .activeCount(counts.getActive())
                    .inactiveCount(counts.getInactive())
                    .resignedCount(counts.getResigned())
                    .kind(OfficerList.KindEnum.OFFICER_LIST)
                    .startIndex(startIndex)
                    .itemsPerPage(itemsPerPage)
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
