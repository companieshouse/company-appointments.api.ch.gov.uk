package uk.gov.companieshouse.company_appointments.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNull;

class CustomDeserializerTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .registerModule(new SimpleModule().addDeserializer(String.class, new CustomDeserializer()));

    @Test
    void successfullyDeserialize() throws Exception {
        // given
        String json = IOUtils.resourceToString("/PUT_full_record_request_body_with_empty_locality_fields.json",
                                                StandardCharsets.UTF_8);

        // when
        final FullRecordCompanyOfficerApi requestModel = objectMapper.readValue(json, FullRecordCompanyOfficerApi.class);

        // then
        assertNull(requestModel.getExternalData().getData().getServiceAddress().getLocality());
        assertNull(requestModel.getExternalData().getSensitiveData().getUsualResidentialAddress().getLocality());
    }
}
