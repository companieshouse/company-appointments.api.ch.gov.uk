package uk.gov.companieshouse.company_appointments.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.company_appointments.model.OfficerMergeMessage;
import uk.gov.companieshouse.company_appointments.serdes.OfficerMergeSerialiser;

@Configuration
public class KafkaProducerConfig {

    private final String bootstrapAddress;

    public KafkaProducerConfig(@Value(value = "${spring.kafka.bootstrap-servers}") String bootstrapAddress) {
        this.bootstrapAddress = bootstrapAddress;
    }

    @Bean
    public ProducerFactory<String, OfficerMergeMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(
                Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OfficerMergeSerialiser.class)
        );
    }

    @Bean
    public KafkaTemplate<String, OfficerMergeMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
