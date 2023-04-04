package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaDateOfBirth;

@Component
public class DeltaDateOfBirthTransformer implements Transformative<DateOfBirth, DeltaDateOfBirth> {

    @Override
    public DeltaDateOfBirth factory() {
        return new DeltaDateOfBirth();
    }

    @Override
    public DeltaDateOfBirth transform(DateOfBirth source, DeltaDateOfBirth entity)
            throws FailedToTransformException {

        try {
            entity.setYear(source.getYear());
            entity.setMonth(source.getMonth());
            entity.setDay(source.getDay());

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform DateOfBirth: %s",
                    e.getMessage()));
        }
    }
}
