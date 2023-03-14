package uk.gov.companieshouse.company_appointments.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class JSONMatcher {

    @Autowired
    private Logger logger;

    public void doJSONsMatch(String expected, String actual) {
        JsonNode expectedJson = stringToJSON(expected);
        JsonNode actualJson = stringToJSON(actual);
        assertThat(expectedJson).isEqualTo(actualJson);
    }

    private JsonNode stringToJSON(String object) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode output;

        try {
            output = mapper.readTree(object);
        } catch (JsonProcessingException e) {
            logger.error("Could not process JSON: " + e);
            output = null;
        }
        return output;
    }
}
