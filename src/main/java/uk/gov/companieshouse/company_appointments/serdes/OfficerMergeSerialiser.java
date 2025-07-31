package uk.gov.companieshouse.company_appointments.serdes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.company_appointments.model.OfficerMergeMessage;

public class OfficerMergeSerialiser implements Serializer<OfficerMergeMessage> {

    @Override
    public byte[] serialize(String topic, OfficerMergeMessage message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<OfficerMergeMessage> writer = getDatumWriter();
        try {
            writer.write(message, encoder);
        } catch (IOException ex) {
            throw new RuntimeException("Error serialising Officer Merge message", ex);
        }
        return outputStream.toByteArray();
    }

    public DatumWriter<OfficerMergeMessage> getDatumWriter() {
        return new ReflectDatumWriter<>(OfficerMergeMessage.class);
    }
}
