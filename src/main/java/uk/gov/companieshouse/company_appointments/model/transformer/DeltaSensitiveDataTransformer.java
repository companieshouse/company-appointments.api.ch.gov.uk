package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;

@Component
public class DeltaSensitiveDataTransformer implements Transformative<SensitiveData, DeltaSensitiveData> {

    private final DeltaUsualResidentialAddressTransformer usualResidentialAddressTransformer;
    private final DeltaDateOfBirthTransformer dateOfBirthTransformer;

    public DeltaSensitiveDataTransformer(
            DeltaUsualResidentialAddressTransformer usualResidentialAddressTransformer,
            DeltaDateOfBirthTransformer dateOfBirthTransformer) {
        this.usualResidentialAddressTransformer = usualResidentialAddressTransformer;
        this.dateOfBirthTransformer = dateOfBirthTransformer;
    }

    @Override
    public DeltaSensitiveData factory() {
        return new DeltaSensitiveData();
    }

    @Override
    public DeltaSensitiveData transform(SensitiveData source, DeltaSensitiveData entity)
            throws FailedToTransformException {

        try {
            entity.setUsualResidentialAddress(usualResidentialAddressTransformer.transform(
                    source.getUsualResidentialAddress()));
            entity.setDateOfBirth(dateOfBirthTransformer.transform(source.getDateOfBirth()));
            entity.setResidentialAddressSameAsServiceAddress(source.getResidentialAddressSameAsServiceAddress());

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform SensitiveData: %s",
                    e.getMessage()));
        }
    }
}
