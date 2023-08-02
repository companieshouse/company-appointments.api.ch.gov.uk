package uk.gov.companieshouse.company_appointments.roles;

import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

public class RoleHelper {

    private RoleHelper() {
    }

    public static boolean isSecretary(CompanyAppointmentDocument companyAppointment) {
        return SecretarialRoles.stream()
                .anyMatch(s -> s.getRole().equals(companyAppointment.getData().getOfficerRole()));
    }

    public static boolean isDirector(CompanyAppointmentDocument companyAppointment) {
        return DirectorRoles.stream().anyMatch(d -> d.getRole().equals(companyAppointment.getData().getOfficerRole()));
    }

    public static boolean isLlpMember(CompanyAppointmentDocument companyAppointment) {
        return LlpRoles.stream().anyMatch(l -> l.getRole().equals(companyAppointment.getData().getOfficerRole()));
    }

    public static boolean isRegisterType(CompanyAppointmentDocument companyAppointment, String registerType) {
        return registerType.equals("directors") && isDirector(companyAppointment)
                || registerType.equals("secretaries") && isSecretary(companyAppointment)
                || registerType.equals("llp_members") && isLlpMember(companyAppointment);
    }
}
