package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.roles.DirectorRoles;
import uk.gov.companieshouse.company_appointments.roles.LlpRoles;
import uk.gov.companieshouse.company_appointments.roles.RoleHelper;
import uk.gov.companieshouse.company_appointments.roles.SecretarialRoles;

@ExtendWith(MockitoExtension.class)
class RoleHelperTest {

    @Test
    void testIsDirectorTrue() {
        for (String role : DirectorRoles.stream().map(DirectorRoles::getRole).collect(Collectors.toList())) {
            DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole(role).build();
            CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
            boolean result = RoleHelper.isDirector(appointmentDocument);
            assertTrue(result);
        }
    }

    @Test
    void testIsSecretariesTrue() {
        for (String role : SecretarialRoles.stream().map(SecretarialRoles::getRole).collect(Collectors.toList())) {
            DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole(role).build();
            CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
            boolean result = RoleHelper.isSecretary(appointmentDocument);
            assertTrue(result);
        }
    }

    @Test
    void testIsLlpMemberTrue() {
        for (String role : LlpRoles.stream().map(LlpRoles::getRole).collect(Collectors.toList())) {
            DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole(role).build();
            CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
            boolean result = RoleHelper.isLlpMember(appointmentDocument);
            assertTrue(result);
        }
    }

    @Test
    void testIsDirectorFalse() {
        DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole("not director").build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
        boolean result = RoleHelper.isDirector(appointmentDocument);
        assertFalse(result);
    }

    @Test
    void testIsSecretaryFalse() {
        DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole("not secretary").build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
        boolean result = RoleHelper.isSecretary(appointmentDocument);
        assertFalse(result);
    }

    @Test
    void testIsLlpMemberFalse() {
        DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole("not llp member").build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
        boolean result = RoleHelper.isLlpMember(appointmentDocument);
        assertFalse(result);
    }

    @Test
    void testIsRegisterTypeDirectorTrue() {
        for (String role : DirectorRoles.stream().map(DirectorRoles::getRole).collect(Collectors.toList())) {
            DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole(role).build();
            CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
            boolean result = RoleHelper.isRegisterType(appointmentDocument, "directors");
            assertTrue(result);
        }
    }

    @Test
    void testIsRegisterTypeSecretariesTrue() {
        for (String role : SecretarialRoles.stream().map(SecretarialRoles::getRole).collect(Collectors.toList())) {
            DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole(role).build();
            CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
            boolean result = RoleHelper.isRegisterType(appointmentDocument, "secretaries");
            assertTrue(result);
        }
    }

    @Test
    void testIsRegisterTypeLlpMemberTrue() {
        for (String role : LlpRoles.stream().map(LlpRoles::getRole).collect(Collectors.toList())) {
            DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole(role).build();
            CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
            boolean result = RoleHelper.isRegisterType(appointmentDocument, "llp_members");
            assertTrue(result);
        }
    }

    @Test
    void testIsRegisterTypeInvalidReturnsFalse() {
        DeltaOfficerData officerData = DeltaOfficerData.Builder.builder().officerRole("director").build();
        CompanyAppointmentDocument appointmentDocument = buildCompanyAppointmentDocument(officerData);
        boolean result = RoleHelper.isRegisterType(appointmentDocument, "invalid");
        assertFalse(result);
    }

    private CompanyAppointmentDocument buildCompanyAppointmentDocument(DeltaOfficerData data) {
        return new CompanyAppointmentDocument()
                .id("1")
                .data(data)
                .companyStatus("active");
    }
}
