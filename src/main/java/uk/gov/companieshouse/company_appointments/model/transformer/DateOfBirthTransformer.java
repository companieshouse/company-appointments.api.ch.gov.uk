package uk.gov.companieshouse.company_appointments.model.transformer;

import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirth;

public class DateOfBirthTransformer implements Transformative<uk.gov.companieshouse.api.appointment.DateOfBirth, DateOfBirth>{

    public DateOfBirth factory() {
        return new DateOfBirth();
    }

    public DateOfBirth transform(uk.gov.companieshouse.api.appointment.DateOfBirth doB, DateOfBirth newDoB) throws NonRetryableErrorException {

        newDoB.setDay(doB.getDay());
        newDoB.setMonth(doB.getMonth());
        newDoB.setYear(doB.getYear());

        return newDoB;
    }
}
