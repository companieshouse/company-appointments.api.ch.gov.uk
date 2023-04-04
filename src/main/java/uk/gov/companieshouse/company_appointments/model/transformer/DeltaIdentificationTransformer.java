package uk.gov.companieshouse.company_appointments.model.transformer;

import static uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification.IdentificationTypeEnum.fromValue;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;

@Component
public class DeltaIdentificationTransformer implements Transformative<Identification, DeltaIdentification> {

    @Override
    public DeltaIdentification factory() {
        return new DeltaIdentification();
    }

    @Override
    public DeltaIdentification transform(Identification source, DeltaIdentification entity) throws FailedToTransformException {

        try {
            entity.setIdentificationType(fromValue(source.getIdentificationType().getValue()));
            entity.setLegalAuthority(source.getLegalAuthority());
            entity.setLegalForm(source.getLegalForm());
            entity.setPlaceRegistered(source.getPlaceRegistered());
            entity.setRegistrationNumber(source.getRegistrationNumber());

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform Identification: %s", e.getMessage()));
        }
    }
}
