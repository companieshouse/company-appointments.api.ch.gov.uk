package uk.gov.companieshouse.company_appointments.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
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
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.FetchAppointmentsRequest;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.CompanyStatus;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.company_appointments.util.CompanyStatusValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class CompanyAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);
    private static final int DEFAULT_ITEMS_PER_PAGE = 35;
    private static final int DEFAULT_START_INDEX = 0;
    private static final String ACTIVE = "active";

    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final CompanyAppointmentMapper companyAppointmentMapper;
    private final CompanyStatusValidator companyStatusValidator;
    private final CompanyRegisterService companyRegisterService;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final Clock clock;

    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper,
            CompanyRegisterService companyRegisterService,
            CompanyMetricsApiService companyMetricsApiService,
            CompanyStatusValidator companyStatusValidator,
            Clock clock) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
        this.companyRegisterService = companyRegisterService;
        this.companyMetricsApiService = companyMetricsApiService;
        this.companyStatusValidator = companyStatusValidator;
        this.clock = clock;
    }

    public OfficerSummary fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        LOGGER.debug(String.format("Fetching appointment [%s] for company [%s]", appointmentID, companyNumber),
                DataMapHolder.getLogMap());
        CompanyAppointmentDocument appointmentDocument = companyAppointmentRepository.readByCompanyNumberAndAppointmentID(
                        companyNumber, appointmentID)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointment [%s] for company [%s] not found", appointmentID, companyNumber)));
        return companyAppointmentMapper.map(appointmentDocument)
                .etag(appointmentDocument.getData().getEtag());
    }

    public OfficerList fetchAppointmentsForCompany(FetchAppointmentsRequest request)
            throws NotFoundException, ServiceUnavailableException {
        String companyNumber = request.getCompanyNumber();
        String orderBy = request.getOrderBy();
        String filter = request.getFilter();
        String registerType = request.getRegisterType();
        int startIndex = Optional.ofNullable(request.getStartIndex())
                .orElse(DEFAULT_START_INDEX);
        int itemsPerPage = Optional.ofNullable(request.getItemsPerPage())
                .orElse(DEFAULT_ITEMS_PER_PAGE);
        final boolean registerView = request.getRegisterView() != null && request.getRegisterView();
        boolean filterEnabled = checkFilterEnabled(filter);

        LOGGER.debug(
                String.format("Fetching appointments for company [%s] with order by [%s]", companyNumber, orderBy),
                DataMapHolder.getLogMap());

        List<CompanyAppointmentDocument> allAppointmentData = companyAppointmentRepository.getCompanyAppointments(
                companyNumber, orderBy, registerType, startIndex, itemsPerPage, registerView,
                filterEnabled);

        if (allAppointmentData.isEmpty()) {
            return new OfficerList()
                    .totalResults(0)
                    .items(Collections.emptyList())
                    .activeCount(0)
                    .inactiveCount(0)
                    .resignedCount(0)
                    .kind(OfficerList.KindEnum.OFFICER_LIST)
                    .startIndex(startIndex)
                    .itemsPerPage(itemsPerPage)
                    .links(new LinkTypes())
                    .etag("");
        }

        MetricsApi metricsApi = companyMetricsApiService.invokeGetMetricsApi(companyNumber).getData();

        if (registerView && !companyRegisterService.isRegisterHeldInCompaniesHouse(registerType,
                metricsApi.getRegisters())) {
            throw new NotFoundException("Register not held at Companies House");
        }

        AppointmentsApi appointmentsCounts = Optional.ofNullable(metricsApi.getCounts())
                .map(CountsApi::getAppointments)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Appointments metrics for company number [%s] not found", companyNumber)));

        Counts counts = registerView ? new Counts(appointmentsCounts, registerType) :
                new Counts(appointmentsCounts, allAppointmentData.getFirst().getCompanyStatus(), filterEnabled);

        List<OfficerSummary> officerSummaries = allAppointmentData.stream()
                .map(appointment -> companyAppointmentMapper.map(appointment, registerView))
                .toList();

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
                .etag(allAppointmentData.getFirst().getData().getEtag());
    }

    public void patchCompanyNameStatus(String companyNumber, String companyName,
            String companyStatus)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (isBlank(companyName) || isBlank(companyStatus)) {
            LOGGER.error("Company name and/or company status missing", DataMapHolder.getLogMap());
            throw new BadRequestException("Company name and/or company status missing");
        }
        if (!companyStatusValidator.isValidCompanyStatus(companyStatus)) {
            LOGGER.error("Invalid company status provided", DataMapHolder.getLogMap());
            throw new BadRequestException("Invalid company status provided");
        }

        LOGGER.info("Patching company name and company status for appointment", DataMapHolder.getLogMap());

        try {
            long updatedCount = companyAppointmentRepository.patchAppointmentNameStatusInCompany(
                    companyNumber,
                    companyName, companyStatus, Instant.now(clock),
                    GenerateEtagUtil.generateEtag());
            if (updatedCount == 0) {
                LOGGER.info("No appointments found for company during PATCH request", DataMapHolder.getLogMap());
                throw new NotFoundException("No appointments found for company during PATCH request");
            }
            LOGGER.info("Appointments for company updated successfully", DataMapHolder.getLogMap());
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when patching company name/status", DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Recoverable MongoDB error when patching company name/status", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when patching company name/status", DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("MongoDB error when patching company name/status", ex);
        }
    }

    private boolean checkFilterEnabled(String filter) {
        if (StringUtils.isNotBlank(filter)) {
            if (ACTIVE.equals(filter)) {
                return true;
            } else {
                final String msg = String.format("Invalid filter parameter supplied: %s", filter);
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new BadRequestException(msg);
            }
        } else {
            return false;
        }
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
                    throw new BadRequestException(
                            "Incorrect register type, must be directors, secretaries or llp_members");
            }
            inactive = null;
        }

        private Counts(final AppointmentsApi appointments, final String status, final boolean isFilterEnabled) {
            CompanyStatus companyStatus = CompanyStatus.fromValue(status);
            switch (companyStatus) {
                case CLOSED:
                case DISSOLVED:
                case CONVERTED_CLOSED:
                    active = 0;
                    inactive = appointments.getActiveCount();
                    break;
                default:
                    active = appointments.getActiveCount();
                    inactive = 0;

            }
            totalResults = isFilterEnabled ? appointments.getActiveCount() : appointments.getTotalCount();
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
