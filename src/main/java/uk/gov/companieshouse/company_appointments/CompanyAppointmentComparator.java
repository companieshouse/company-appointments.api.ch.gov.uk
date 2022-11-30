package uk.gov.companieshouse.company_appointments;

import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

import java.util.Comparator;

public class CompanyAppointmentComparator implements Comparator<CompanyAppointmentView> {

    private boolean isSecretary(CompanyAppointmentView companyAppointmentView) {
        return SecretarialRoles.stream().anyMatch(s -> s.getRole().equals(companyAppointmentView.getOfficerRole()));
    }

    @Override
    public int compare(CompanyAppointmentView o1, CompanyAppointmentView o2) {

        if (o1.getResignedOn() == null && o2.getResignedOn() != null) {
            return -1;
        } else if (o1.getResignedOn() != null && o2.getResignedOn() == null) {
            return 1;
        } else if (isSecretary(o1) && !isSecretary(o2)) {
            return -1;
        } else if(!isSecretary(o1) && isSecretary(o2)) {
            return 1;
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    }
}


