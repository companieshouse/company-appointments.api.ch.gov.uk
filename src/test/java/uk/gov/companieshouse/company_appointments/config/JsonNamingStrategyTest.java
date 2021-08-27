package uk.gov.companieshouse.company_appointments.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.PersistentProperty;

@ExtendWith(MockitoExtension.class)
class JsonNamingStrategyTest {

    private JsonNamingStrategy jsonNamingStrategy;

    @Mock
    private PersistentProperty<?> persistentProperty;

    @Mock
    private JsonProperty jsonProperty;

    @BeforeEach
    void setup() {
        jsonNamingStrategy = new JsonNamingStrategy();
    }

    @Test
    void returnsFieldName() {
        // given
        when(persistentProperty.findAnnotation(JsonProperty.class)).thenReturn(jsonProperty);

        // when
        String result = jsonNamingStrategy.getFieldName(persistentProperty);

        // then
        assertEquals(null, result);
    }

    @Test
     void returnsFieldNameThrows() {
        // given
        when(persistentProperty.findAnnotation(JsonProperty.class)).thenThrow(new RuntimeException());

        // then
        assertThrows(IllegalArgumentException.class, () -> {jsonNamingStrategy.getFieldName(persistentProperty);});
    }

}