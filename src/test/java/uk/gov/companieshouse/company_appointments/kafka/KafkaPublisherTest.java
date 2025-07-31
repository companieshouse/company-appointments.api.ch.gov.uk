package uk.gov.companieshouse.company_appointments.kafka;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import uk.gov.companieshouse.company_appointments.exception.BadGatewayException;
import uk.gov.companieshouse.company_appointments.model.OfficerMergeMessage;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherTest {

    private static final String OFFICER_ID = "officer_id";
    private static final String PREVIOUS_OFFICER_ID = "previous_officer_id";
    private static final String CONTEXT_ID = "context_id";
    private static final String OFFICER_MERGE_TOPIC = "officer-merge";

    @Mock
    private KafkaTemplate<String, OfficerMergeMessage> kafkaTemplate;

    private KafkaPublisher kafkaPublisher;

    @Mock
    private SendResult<String, OfficerMergeMessage> sendResult;

    @BeforeEach
    void setup() {
        kafkaPublisher = new KafkaPublisher(kafkaTemplate, OFFICER_MERGE_TOPIC);
    }

    @Test
    void shouldPublishToOfficerMergeTopic() {
        // given
        OfficerMergeMessage officerMergeMessage = OfficerMergeMessage.builder()
                .officerId(OFFICER_ID)
                .previousOfficerId(PREVIOUS_OFFICER_ID)
                .contextId(CONTEXT_ID)
                .build();

        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(sendResult));

        // when
        kafkaPublisher.publishToOfficerMergeTopic(OfficerMergeMessage.builder()
                .officerId(OFFICER_ID)
                .previousOfficerId(PREVIOUS_OFFICER_ID)
                .contextId(CONTEXT_ID)
                .build());

        // then
        verify(kafkaTemplate).send(OFFICER_MERGE_TOPIC, officerMergeMessage);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenKafkaExceptionCaught() {
        // given
        OfficerMergeMessage officerMergeMessage = OfficerMergeMessage.builder()
                .officerId(OFFICER_ID)
                .previousOfficerId(PREVIOUS_OFFICER_ID)
                .contextId(CONTEXT_ID)
                .build();

        when(kafkaTemplate.send(anyString(), any())).thenThrow(KafkaException.class);

        // when
        Executable executable = () -> kafkaPublisher.publishToOfficerMergeTopic(OfficerMergeMessage.builder()
                .officerId(OFFICER_ID)
                .previousOfficerId(PREVIOUS_OFFICER_ID)
                .contextId(CONTEXT_ID)
                .build());

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(kafkaTemplate).send(OFFICER_MERGE_TOPIC, officerMergeMessage);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenCompletableFutureFails() {
        // given
        OfficerMergeMessage officerMergeMessage = OfficerMergeMessage.builder()
                .officerId(OFFICER_ID)
                .previousOfficerId(PREVIOUS_OFFICER_ID)
                .contextId(CONTEXT_ID)
                .build();

        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));

        // when
        Executable executable = () -> kafkaPublisher.publishToOfficerMergeTopic(OfficerMergeMessage.builder()
                .officerId(OFFICER_ID)
                .previousOfficerId(PREVIOUS_OFFICER_ID)
                .contextId(CONTEXT_ID)
                .build());

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(kafkaTemplate).send(OFFICER_MERGE_TOPIC, officerMergeMessage);
    }
}