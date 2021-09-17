package uk.gov.companieshouse.company_appointments.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.lang.NonNull;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.stereotype.Component;

@Component
public class JsonNamingStrategy extends SnakeCaseFieldNamingStrategy {

    @Autowired
    public JsonNamingStrategy() {
    }

    @Override
    @NonNull
    public String getFieldName(@NonNull PersistentProperty<?> property) {
        try {
            JsonProperty jsonProperty =
                Objects.requireNonNull(property.findAnnotation(JsonProperty.class));
            return jsonProperty.value();
        } catch (Throwable e) {
            return super.getFieldName(property);
        }
    }
}