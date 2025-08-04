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
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.officermerge.OfficerMerge;

@ExtendWith(MockitoExtension.class)
class OfficerMergeKafkaProducerTest {

    private static final String OFFICER_ID = "officer_id";
    private static final String PREVIOUS_OFFICER_ID = "previous_officer_id";
    private static final String CONTEXT_ID = "context_id";
    private static final String OFFICER_MERGE_TOPIC = "officer-merge";

    @Mock
    private KafkaTemplate<String, OfficerMerge> kafkaTemplate;

    private OfficerMergeKafkaProducer officerMergeKafkaProducer;

    @Mock
    private SendResult<String, OfficerMerge> sendResult;

    @BeforeEach
    void setup() {
        officerMergeKafkaProducer = new OfficerMergeKafkaProducer(kafkaTemplate, OFFICER_MERGE_TOPIC);
    }

    @Test
    void shouldInvokeOfficerMerge() {
        // given
        OfficerMerge officerMerge = new OfficerMerge(OFFICER_ID, PREVIOUS_OFFICER_ID, CONTEXT_ID);

        DataMapHolder.get().requestId(CONTEXT_ID);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(sendResult));

        // when
        officerMergeKafkaProducer.invokeOfficerMerge(OFFICER_ID, PREVIOUS_OFFICER_ID);

        // then
        verify(kafkaTemplate).send(OFFICER_MERGE_TOPIC, officerMerge);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenKafkaExceptionCaught() {
        // given
        OfficerMerge officerMerge = new OfficerMerge(OFFICER_ID, PREVIOUS_OFFICER_ID, CONTEXT_ID);

        DataMapHolder.get().requestId(CONTEXT_ID);
        when(kafkaTemplate.send(anyString(), any())).thenThrow(KafkaException.class);

        // when
        Executable executable = () -> officerMergeKafkaProducer.invokeOfficerMerge(OFFICER_ID, PREVIOUS_OFFICER_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(kafkaTemplate).send(OFFICER_MERGE_TOPIC, officerMerge);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenCompletableFutureFails() {
        // given
        OfficerMerge officerMerge = new OfficerMerge(OFFICER_ID, PREVIOUS_OFFICER_ID, CONTEXT_ID);

        DataMapHolder.get().requestId(CONTEXT_ID);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));

        // when
        Executable executable = () -> officerMergeKafkaProducer.invokeOfficerMerge(OFFICER_ID, PREVIOUS_OFFICER_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(kafkaTemplate).send(OFFICER_MERGE_TOPIC, officerMerge);
    }
}