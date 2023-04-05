package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.company_appointments.model.data.DeltaDateOfBirth;

@Component
class DeltaDateOfBirthTransformer implements Transformative<DateOfBirth, DeltaDateOfBirth> {

    @Override
    public DeltaDateOfBirth factory() {
        return new DeltaDateOfBirth();
    }

    @Override
    public DeltaDateOfBirth transform(DateOfBirth source, DeltaDateOfBirth entity) {

        entity.setYear(source.getYear());
        entity.setMonth(source.getMonth());
        entity.setDay(source.getDay());

        return entity;
    }
}
