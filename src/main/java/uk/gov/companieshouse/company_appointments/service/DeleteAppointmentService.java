package uk.gov.companieshouse.company_appointments.service;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.ConflictException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.DeleteAppointmentParameters;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeleteAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);
    private static final DateTimeFormatter DELTA_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final ResourceChangedApiService resourceChangedApiService;
    private final ResourceChangedDataCleaner resourceChangedDataCleaner;

    public DeleteAppointmentService(
            CompanyAppointmentRepository companyAppointmentRepository,
            ResourceChangedApiService resourceChangedApiService,
            ResourceChangedDataCleaner resourceChangedDataCleaner) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.resourceChangedDataCleaner = resourceChangedDataCleaner;
    }

    public void deleteAppointment(DeleteAppointmentParameters deleteAppointmentParameters) {
        final String requestDeltaAt = deleteAppointmentParameters.deltaAt();
        final String appointmentId = deleteAppointmentParameters.appointmentId();
        final String companyNumber = deleteAppointmentParameters.companyNumber();
        final String officerId = deleteAppointmentParameters.officerId();

        if (StringUtils.isBlank(requestDeltaAt)) {
            LOGGER.error("deltaAt is null or empty", DataMapHolder.getLogMap());
            throw new BadRequestException("deltaAt is null or empty");
        }

        try {
            companyAppointmentRepository.readByCompanyNumberAndID(companyNumber, appointmentId)
                    .ifPresentOrElse(document -> {
                        LOGGER.info("Appointment found", DataMapHolder.getLogMap());

                        Instant requestDeltaAtInstant = LocalDateTime.parse(requestDeltaAt, DELTA_AT_FORMATTER)
                                .toInstant(UTC);
                        if (requestDeltaAtInstant.isBefore(document.getDeltaAt())) {
                            LOGGER.error("Delta at on request is stale", DataMapHolder.getLogMap());
                            throw new ConflictException("Delta at on request is stale");
                        } else {
                            LOGGER.info("Deleting appointment", DataMapHolder.getLogMap());
                            companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);
                            publishResourceChanged(companyNumber, appointmentId,
                                    resourceChangedDataCleaner.cleanOutNullValues(document));
                        }
                    }, () -> {
                        LOGGER.info(
                                "Appointment not found - publishing resource changed message with appointments link only",
                                DataMapHolder.getLogMap());

                        final String appointmentsUri = "/officers/%s/appointments".formatted(officerId);
                        OfficerSummary officerSummary = new OfficerSummary()
                                .links(new ItemLinkTypes()
                                        .officer(new OfficerLinkTypes()
                                                .appointments(appointmentsUri)));

                        publishResourceChanged(companyNumber, appointmentId,
                                resourceChangedDataCleaner.cleanOutNullValues(officerSummary));
                    });
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when deleting appointment", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when deleting appointment", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when deleting appointment", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when deleting appointment", ex);
        }
    }

    private void publishResourceChanged(String companyNumber, String appointmentId, Object officersData) {
        ResourceChangedRequest resourceChangedRequest = ResourceChangedRequest.builder()
                .contextId(DataMapHolder.getRequestId())
                .companyNumber(companyNumber)
                .appointmentId(appointmentId)
                .officerData(officersData)
                .delete(true)
                .build();
        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);
        LOGGER.info("ChsKafka api DELETED invoked", DataMapHolder.getLogMap());
    }
}

