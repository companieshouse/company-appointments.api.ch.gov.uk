package uk.gov.companieshouse.company_appointments.util;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;

class EmptyFieldDeserializerTest {

    private final JsonMapper objectMapper = JsonMapper.builderWithJackson2Defaults()
            .addModule(new SimpleModule().addDeserializer(String.class, new EmptyFieldDeserializer()))
            .build();

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

    @Test
    void deserializeObjectOrArrayAsNullRatherThanFailing() {
        // when
        final Wrapper fromObject = objectMapper.readValue("{\"value\":{\"a\":1}}", Wrapper.class);
        final Wrapper fromArray = objectMapper.readValue("{\"value\":[1,2]}", Wrapper.class);

        // then
        assertNull(fromObject.value());
        assertNull(fromArray.value());
    }

    private record Wrapper(String value) {
    }
}
