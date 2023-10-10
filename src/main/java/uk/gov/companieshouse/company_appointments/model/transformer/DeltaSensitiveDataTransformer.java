package uk.gov.companieshouse.company_appointments.model.transformer;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;

@Component
public class DeltaSensitiveDataTransformer implements Transformative<SensitiveData, DeltaSensitiveData> {

    private final DeltaUsualResidentialAddressTransformer usualResidentialAddressTransformer;

    public DeltaSensitiveDataTransformer(
            DeltaUsualResidentialAddressTransformer usualResidentialAddressTransformer) {
        this.usualResidentialAddressTransformer = usualResidentialAddressTransformer;
    }

    @Override
    public DeltaSensitiveData factory() {
        return new DeltaSensitiveData();
    }

    @Override
    public DeltaSensitiveData transform(SensitiveData source, DeltaSensitiveData entity)
            throws FailedToTransformException {

        try {
            entity.setUsualResidentialAddress(source.getUsualResidentialAddress() != null ?
                    usualResidentialAddressTransformer.transform(source.getUsualResidentialAddress()) : null);
            entity.setDateOfBirth(source.getDateOfBirth() != null ?
                    Instant.from(LocalDate.of(
                                    source.getDateOfBirth().getYear(),
                                    source.getDateOfBirth().getMonth(),
                                    source.getDateOfBirth().getDay())
                            .atStartOfDay(UTC))
                    : null);
            entity.setResidentialAddressIsSameAsServiceAddress(source.getResidentialAddressSameAsServiceAddress());

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform SensitiveData: %s",
                    e.getMessage()));
        }
    }
}
