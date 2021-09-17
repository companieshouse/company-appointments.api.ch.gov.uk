package uk.gov.companieshouse.company_appointments.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.PersistentProperty;

@ExtendWith(MockitoExtension.class)
class JsonSnakeCaseNamingStrategyTest {

    private JsonSnakeCaseNamingStrategy jsonSnakeCaseNamingStrategy;

    @Mock
    private PersistentProperty<?> persistentProperty;

    @Mock
    private JsonProperty jsonProperty;

    @BeforeEach
    void setup() {
        jsonSnakeCaseNamingStrategy = new JsonSnakeCaseNamingStrategy();
    }

    @Test
    void getFieldNameWhenJsonPropertyPresentThenOverridesSnakeCaseStrategy() {
        // given
        when(persistentProperty.findAnnotation(JsonProperty.class)).thenReturn(jsonProperty);
        when(jsonProperty.value()).thenReturn("json_property_4");

        // when
        String result = jsonSnakeCaseNamingStrategy.getFieldName(persistentProperty);

        // then
        assertThat(result, is("json_property_4"));
    }

    @Test
    void getFieldNameWhenJsonPropertyAbsentThenNoUnderscoreBeforeDigit() {
        // given
        when(persistentProperty.findAnnotation(JsonProperty.class)).thenReturn(null);
        when(persistentProperty.getName()).thenReturn("JsonProperty4");

        // when
        String result = jsonSnakeCaseNamingStrategy.getFieldName(persistentProperty);

        // then
        assertThat(result, is("json_property4"));
    }

}