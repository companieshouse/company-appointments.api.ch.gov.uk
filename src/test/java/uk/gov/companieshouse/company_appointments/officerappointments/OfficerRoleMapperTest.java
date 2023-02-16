package uk.gov.companieshouse.company_appointments.officerappointments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;

import static org.junit.jupiter.api.Assertions.*;

class OfficerRoleMapperTest {

    @Test
    void mapOfficerRole() {
        // given
        String officerRole = "director";

        // when
        OfficerRoleEnum actual = OfficerRoleMapper.mapOfficerRole(officerRole);

        // then
        assertEquals(OfficerRoleEnum.DIRECTOR, actual);
    }

    @Test
    void mapOfficerRoleNull() {
        // given
        // when
        OfficerRoleEnum actual = OfficerRoleMapper.mapOfficerRole(null);

        // then
        assertNull(actual);
    }

    @Test
    void mapOfficerRoleInvalidRole() {
        // given
        String officerRole = "invalid role";

        // when
        Executable executable = () -> OfficerRoleMapper.mapOfficerRole(officerRole);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Unexpected value '%s'", officerRole), exception.getMessage());
    }
}