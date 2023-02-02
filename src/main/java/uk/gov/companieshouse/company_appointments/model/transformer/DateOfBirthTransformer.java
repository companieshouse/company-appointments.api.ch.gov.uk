package uk.gov.companieshouse.company_appointments.model.transformer;

import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirthView;

public class DateOfBirthTransformer implements Transformative<DateOfBirth, DateOfBirthView>{

    public DateOfBirthView factory() {
        return new DateOfBirthView();
    }

    public DateOfBirthView transform(DateOfBirth doB, DateOfBirthView newDoB) throws NonRetryableErrorException {

        newDoB.setDay(doB.getDay());
        newDoB.setMonth(doB.getMonth());
        newDoB.setYear(doB.getYear());

        return newDoB;
    }
}
