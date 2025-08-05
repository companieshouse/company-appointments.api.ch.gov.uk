package uk.gov.companieshouse.company_appointments.serdes;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.exception.SerDesException;
import uk.gov.companieshouse.officermerge.OfficerMerge;

@ExtendWith(MockitoExtension.class)
class OfficerMergeSerialiserTest {

    @Mock
    private DatumWriter<OfficerMerge> writer;

    @Test
    void testSerialiseChsDelta() {
        // given
        OfficerMerge delta = new OfficerMerge("officerId", "previousOfficerId", "contextId");
        try (OfficerMergeSerialiser serialiser = new OfficerMergeSerialiser()) {

            // when
            byte[] actual = serialiser.serialize("topic", delta);

            // then
            assertThat(actual, is(notNullValue()));
        }
    }

    @Test
    void testThrowNonRetryableExceptionIfIOExceptionThrown() throws IOException {
        // given
        OfficerMerge delta = new OfficerMerge("officerId", "previousOfficerId", "contextId");
        OfficerMergeSerialiser serialiser = spy(new OfficerMergeSerialiser());
        when(serialiser.getDatumWriter()).thenReturn(writer);
        doThrow(IOException.class).when(writer).write(any(), any());

        // when
        Executable actual = () -> serialiser.serialize("topic", delta);

        // then
        SerDesException exception = assertThrows(SerDesException.class, actual);
        assertThat(exception.getCause(), is(instanceOf(IOException.class)));
    }
}
