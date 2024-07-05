package uk.gov.companieshouse.company_appointments.api;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "seeding_collection_enabled")
public class ResourceChangedApiServiceAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAME_SPACE);

    @Around("@annotation(StreamEvents)")
    public Object checkStreamEventsEnabled() {
            LOGGER.debug("Stream events disabled; not publishing change to chs-kafka-api",
                    DataMapHolder.getLogMap());
            return null;
    }
}
