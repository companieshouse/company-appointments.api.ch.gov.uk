package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary.OfficerRoleEnum;

class OfficerRoleMapperTest {

    private OfficerRoleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OfficerRoleMapper();
    }

    @Test
    void mapOfficerRole() {
        // given
        String officerRole = "director";

        // when
        OfficerRoleEnum actual = mapper.mapOfficerRole(officerRole);

        // then
        assertEquals(OfficerRoleEnum.DIRECTOR, actual);
    }

    @Test
    void mapOfficerRoleCorporateMember() {
        // given
        String officerRole = "corporate-member-of-a-management-organ";

        // when
        OfficerRoleEnum actual = mapper.mapOfficerRole(officerRole);

        // then
        assertEquals(OfficerRoleEnum.CORPORATE_MEMBER_OF_A_MANAGEMENT_ORGAN, actual);
    }

    @Test
    void mapOfficerRoleMember() {
        // given
        String officerRole = "member-of-a-management-organ";

        // when
        OfficerRoleEnum actual = mapper.mapOfficerRole(officerRole);

        // then
        assertEquals(OfficerRoleEnum.MEMBER_OF_A_MANAGEMENT_ORGAN, actual);
    }

    @Test
    void mapOfficerRoleNull() {
        // given
        // when
        OfficerRoleEnum actual = mapper.mapOfficerRole(null);

        // then
        assertNull(actual);
    }

    @Test
    void mapOfficerRoleInvalidCorporateMember() {
        // given
        String officerRole = "corporate-member-of-a-management";

        // when
        Executable executable = () -> mapper.mapOfficerRole(officerRole);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Unexpected value '%s'", officerRole), exception.getMessage());
    }

    @Test
    void mapOfficerRoleInvalidMember() {
        // given
        String officerRole = "member-of-a-management";

        // when
        Executable executable = () -> mapper.mapOfficerRole(officerRole);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Unexpected value '%s'", officerRole), exception.getMessage());
    }

    @Test
    void mapOfficerRoleInvalidRole() {
        // given
        String officerRole = "invalid-role";

        // when
        Executable executable = () -> mapper.mapOfficerRole(officerRole);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Unexpected value '%s'", officerRole), exception.getMessage());
    }

    @Test
    void mapIsCorporateOfficer() {
        // given
        String officerRole = "corporate-director";

        // when
        boolean actual = mapper.mapIsCorporateOfficer(officerRole);

        // then
        assertTrue(actual);
    }

    @Test
    void mapIsCorporateOfficerFalse() {
        // given
        String officerRole = "director";

        // when
        boolean actual = mapper.mapIsCorporateOfficer(officerRole);

        // then
        assertFalse(actual);
    }

    @Test
    void mapIsCorporateOfficerNull() {
        // given
        // when
        boolean actual = mapper.mapIsCorporateOfficer(null);

        // then
        assertFalse(actual);
    }
}