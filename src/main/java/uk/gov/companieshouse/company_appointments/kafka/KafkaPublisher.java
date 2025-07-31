package uk.gov.companieshouse.company_appointments.kafka;

import static uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication.APPLICATION_NAME_SPACE;

import java.util.concurrent.CompletionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.OfficerMergeMessage;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class KafkaPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final KafkaTemplate<String, OfficerMergeMessage> kafkaTemplate;
    private final String officerMergeTopic;

    public KafkaPublisher(KafkaTemplate<String, OfficerMergeMessage> kafkaTemplate,
            @Value("${kafka.officer-merge.topic}") String officerMergeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.officerMergeTopic = officerMergeTopic;
    }

    public void publishToOfficerMergeTopic(OfficerMergeMessage message) {
        try {
            kafkaTemplate.send(officerMergeTopic, message).join();
        } catch (CompletionException ex) {
            final String msg = "Completion error occurred when completing the Kafka send future";
            LOGGER.error(msg);
            throw new BadGatewayException(msg, ex);
        } catch (KafkaException ex) {
            final String msg = "Error occurred when publishing to the officer-merge topic";
            LOGGER.error(msg);
            throw new BadGatewayException(msg, ex);
        }
        LOGGER.info("Successfully published message to the officer-merge topic", DataMapHolder.getLogMap());
    }
}
