package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;
import uk.gov.companieshouse.company_appointments.roles.DirectorRoles;
import uk.gov.companieshouse.company_appointments.roles.LlpRoles;
import uk.gov.companieshouse.company_appointments.roles.RoleHelper;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;

@ExtendWith(MockitoExtension.class)
class RoleHelperTest {

    @Test
    void testIsDirectorTrue() {
        for(String role: DirectorRoles.stream().map(DirectorRoles::getRole).collect(Collectors.toList())){
            OfficerData officerData = OfficerData.builder().withOfficerRole(role).build();
            CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
            boolean result = RoleHelper.isDirector(companyAppointmentData);
            assertTrue(result);
        }
    }

    @Test
    void testIsSecretariesTrue() {
        for(String role: SecretarialRoles.stream().map(SecretarialRoles::getRole).collect(Collectors.toList())){
            OfficerData officerData = OfficerData.builder().withOfficerRole(role).build();
            CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
            boolean result = RoleHelper.isSecretary(companyAppointmentData);
            assertTrue(result);
        }
    }

    @Test
    void testIsLlpMemberTrue() {
        for(String role: LlpRoles.stream().map(LlpRoles::getRole).collect(Collectors.toList())){
            OfficerData officerData = OfficerData.builder().withOfficerRole(role).build();
            CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
            boolean result = RoleHelper.isLlpMember(companyAppointmentData);
            assertTrue(result);
        }
    }

    @Test
    void testIsDirectorFalse() {
        OfficerData officerData = OfficerData.builder().withOfficerRole("not director").build();
        CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
        boolean result = RoleHelper.isDirector(companyAppointmentData);
        assertFalse(result);
    }

    @Test
    void testIsSecretaryFalse() {
        OfficerData officerData = OfficerData.builder().withOfficerRole("not secretary").build();
        CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
        boolean result = RoleHelper.isSecretary(companyAppointmentData);
        assertFalse(result);
    }

    @Test
    void testIsLlpMemberFalse() {
        OfficerData officerData = OfficerData.builder().withOfficerRole("not llp member").build();
        CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
        boolean result = RoleHelper.isLlpMember(companyAppointmentData);
        assertFalse(result);
    }

    @Test
    void testIsRegisterTypeDirectorTrue() {
        for(String role: DirectorRoles.stream().map(DirectorRoles::getRole).collect(Collectors.toList())){
            OfficerData officerData = OfficerData.builder().withOfficerRole(role).build();
            CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
            boolean result = RoleHelper.isRegisterType(companyAppointmentData, "directors");
            assertTrue(result);
        }
    }

    @Test
    void testIsRegisterTypeSecretariesTrue() {
        for(String role: SecretarialRoles.stream().map(SecretarialRoles::getRole).collect(Collectors.toList())){
            OfficerData officerData = OfficerData.builder().withOfficerRole(role).build();
            CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
            boolean result = RoleHelper.isRegisterType(companyAppointmentData, "secretaries");
            assertTrue(result);
        }
    }

    @Test
    void testIsRegisterTypeLlpMemberTrue() {
        for(String role: LlpRoles.stream().map(LlpRoles::getRole).collect(Collectors.toList())){
            OfficerData officerData = OfficerData.builder().withOfficerRole(role).build();
            CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
            boolean result = RoleHelper.isRegisterType(companyAppointmentData, "llp_members");
            assertTrue(result);
        }
    }

    @Test
    void testIsRegisterTypeInvalidReturnsFalse() {
        OfficerData officerData = OfficerData.builder().withOfficerRole("director").build();
        CompanyAppointmentData companyAppointmentData = new CompanyAppointmentData("1", officerData, "active");
        boolean result = RoleHelper.isRegisterType(companyAppointmentData, "invalid");
        assertFalse(result);
    }
}
