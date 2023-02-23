package uk.gov.companieshouse.company_appointments.roles;

import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.roles.DirectorRoles;
import uk.gov.companieshouse.company_appointments.roles.LlpRoles;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;


public class RoleHelper {
    private RoleHelper(){}
    public static boolean isSecretary(CompanyAppointmentData companyAppointmentData){
        return SecretarialRoles.stream().anyMatch(s -> s.getRole().equals(companyAppointmentData.getData().getOfficerRole()));
    }

    public static boolean isDirector(CompanyAppointmentData companyAppointmentData){
        return DirectorRoles.stream().anyMatch(d -> d.getRole().equals(companyAppointmentData.getData().getOfficerRole()));
    }

    public static boolean isLlpMember(CompanyAppointmentData companyAppointmentData){
        return LlpRoles.stream().anyMatch(l -> l.getRole().equals(companyAppointmentData.getData().getOfficerRole()));
    }

    public static boolean isRegisterType(CompanyAppointmentData companyAppointmentData, String registerType) {
        return registerType.equals("directors") && isDirector(companyAppointmentData)
                || registerType.equals("secretaries") && isSecretary(companyAppointmentData)
                || registerType.equals("llp_members") && isLlpMember(companyAppointmentData);
    }
}
