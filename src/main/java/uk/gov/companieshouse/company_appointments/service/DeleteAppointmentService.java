package uk.gov.companieshouse.company_appointments.service;

import static java.time.ZoneOffset.UTC;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.api.ResourceChangedApiService;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.mapper.CompanyAppointmentMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.repository.CompanyAppointmentRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class DeleteAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);
    private static final ObjectMapper NULL_CLEANING_OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(Include.NON_NULL);
    private static final DateTimeFormatter DELTA_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

    private final CompanyAppointmentRepository companyAppointmentRepository;
    private final ResourceChangedApiService resourceChangedApiService;
    private final CompanyAppointmentMapper companyAppointmentMapper;

    public DeleteAppointmentService(
            CompanyAppointmentRepository companyAppointmentRepository,
            ResourceChangedApiService resourceChangedApiService,
            CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.resourceChangedApiService = resourceChangedApiService;
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public void deleteAppointment(String companyNumber, String appointmentId, String deltaAt) {

        if (StringUtils.isBlank(deltaAt)) {
            throw new BadRequestException("deltaAt is null or empty");
        }

        try {
            LOGGER.info("Deleting appointment [%s] for company [%s]".formatted(appointmentId, companyNumber),
                    DataMapHolder.getLogMap());

            Optional<CompanyAppointmentDocument> document = companyAppointmentRepository.readByCompanyNumberAndID(
                    companyNumber, appointmentId);

            if (document.isEmpty()) {

                LOGGER.info("Appointment [%s] for company [%s] not found".formatted(appointmentId, companyNumber),
                        DataMapHolder.getLogMap());
                publishResourceChanged(companyNumber, appointmentId, null);

            } else {

                CompanyAppointmentDocument companyAppointmentDocument = document.get();
                Instant deltaAtInstant = LocalDateTime.parse(deltaAt, DELTA_AT_FORMATTER).toInstant(UTC);

                if (deltaAtInstant.isBefore(companyAppointmentDocument.getDeltaAt())) {
                    logStaleIncomingDelta(companyAppointmentDocument, deltaAtInstant);
                } else {
                    companyAppointmentRepository.deleteByCompanyNumberAndID(companyNumber, appointmentId);
                    publishResourceChanged(companyNumber, appointmentId, cleanDocument(document.get()));
                }
            }
        } catch (DataAccessException e) {
            LOGGER.error(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("%s: %s", e.getClass().getName(), e.getMessage()), DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to chs-kafka-api");
        }
    }

    private void publishResourceChanged(String companyNumber, String appointmentId, Object officersData) {
        resourceChangedApiService.invokeChsKafkaApi(new ResourceChangedRequest(DataMapHolder.getRequestId(),
                companyNumber, appointmentId, officersData, true));
        LOGGER.debug(String.format("ChsKafka api DELETED invoked for appointment [%s] for company [%s]",
                appointmentId, companyNumber), DataMapHolder.getLogMap());
    }

    private Object cleanDocument(CompanyAppointmentDocument document) {
        try {
            String officerJson = NULL_CLEANING_OBJECT_MAPPER.writeValueAsString(
                    companyAppointmentMapper.map(document));
            return NULL_CLEANING_OBJECT_MAPPER.readValue(officerJson, Object.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialise/deserialise officer summary", DataMapHolder.getLogMap());
            throw new UncheckedIOException(e);
        }
    }

    private void logStaleIncomingDelta(final CompanyAppointmentDocument appointmentAPI, final Instant deltaAt) {

        Map<String, Object> logInfo = DataMapHolder.getLogMap();
        logInfo.put("existingDeltaAt", appointmentAPI.getDeltaAt().toString());
        logInfo.put("incomingDeltaAt", deltaAt.toString().isBlank() ? deltaAt.toString() : "No existing delta");
        final String context = appointmentAPI.getAppointmentId();
        LOGGER.errorContext(context, "Received stale delta", null, logInfo);
    }
}

