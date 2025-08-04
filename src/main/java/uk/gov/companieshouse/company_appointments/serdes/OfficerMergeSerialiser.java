package uk.gov.companieshouse.company_appointments.serdes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.company_appointments.exception.SerDesException;
import uk.gov.companieshouse.officermerge.OfficerMerge;

public class OfficerMergeSerialiser implements Serializer<OfficerMerge> {

    @Override
    public byte[] serialize(String topic, OfficerMerge message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<OfficerMerge> writer = new ReflectDatumWriter<>(OfficerMerge.class);
        try {
            writer.write(message, encoder);
        } catch (IOException ex) {
            throw new SerDesException("Error serialising OfficerMerge message", ex);
        }
        return outputStream.toByteArray();
    }
}
