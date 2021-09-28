package uk.gov.companieshouse.company_appointments.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.lang.NonNull;
import java.util.Optional;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.stereotype.Component;

@Component
public class JsonSnakeCaseNamingStrategy extends SnakeCaseFieldNamingStrategy {

    @Override
    @NonNull
    public String getFieldName(@NonNull PersistentProperty<?> property) {
        return Optional.ofNullable(property.findAnnotation(JsonProperty.class))
            .map(JsonProperty::value)
            .orElseGet(() -> super.getFieldName(property));
    }
}