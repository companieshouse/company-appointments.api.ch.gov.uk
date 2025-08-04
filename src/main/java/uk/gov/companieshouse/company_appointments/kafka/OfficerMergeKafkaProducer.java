package uk.gov.companieshouse.company_appointments.kafka;

import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;

import java.util.concurrent.CompletionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officermerge.OfficerMerge;

@Component
public class OfficerMergeKafkaProducer implements OfficerMergeProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final KafkaTemplate<String, OfficerMerge> kafkaTemplate;
    private final String officerMergeTopic;

    public OfficerMergeKafkaProducer(KafkaTemplate<String, OfficerMerge> kafkaTemplate,
            @Value("${kafka.officer-merge.topic}") String officerMergeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.officerMergeTopic = officerMergeTopic;
    }

    public void invokeOfficerMerge(String officerId, String previousOfficerID) {
        try {
            OfficerMerge officerMerge = new OfficerMerge(officerId, previousOfficerID, DataMapHolder.getRequestId());
            kafkaTemplate.send(officerMergeTopic, officerMerge).join();
        } catch (CompletionException ex) {
            final String msg = "Completion error during Kafka send Future";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        } catch (KafkaException ex) {
            final String msg = "Error publishing to officer-merge topic";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        }
        LOGGER.info("Successfully published message to officer-merge topic", DataMapHolder.getLogMap());
    }
}
