package uk.gov.companieshouse.company_appointments.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;

import java.io.IOException;
import java.time.Duration;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import uk.gov.companieshouse.company_appointments.config.TestKafkaConfig;
import uk.gov.companieshouse.company_appointments.kafka.OfficerMergeKafkaProducer;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.officermerge.OfficerMerge;

@SpringBootTest
@Testcontainers
@Import(TestKafkaConfig.class)
class OfficerMergeKafkaProducerIT {

    @Container
    protected static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:latest");

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;
    @Autowired
    private OfficerMergeKafkaProducer kafkaProducer;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldSuccessfullyProduceToOfficerMergeTopic() throws IOException {
        // given
        DataMapHolder.get().requestId("contextId");
        OfficerMerge expected = new OfficerMerge("officerId", "previousOfficerId", "contextId");

        // when
        kafkaProducer.invokeOfficerMerge("officerId", "previousOfficerId");

        // then
        ConsumerRecords<String, byte[]> records = getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        byte[] actualBytes = records.records("officer-merge").iterator().next().value();
        Decoder decoder = DecoderFactory.get().binaryDecoder(actualBytes, null);
        DatumReader<OfficerMerge> reader = new ReflectDatumReader<>(OfficerMerge.class);
        OfficerMerge actual = reader.read(null, decoder);
        assertEquals(expected, actual);
    }
}