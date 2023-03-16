package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import uk.gov.companieshouse.company_appointments.exception.BadRequestException;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SortMapperTest {

    private SortMapper sortMapper;

    @BeforeEach
    void setup() {
        sortMapper = new SortMapper();
    }

    @Test
    void testOfficerRoleSortOrder() throws Exception {

        Sort expected = Sort.by(Sort.Direction.ASC, "officer_role_sort_order")
                .and(Sort.by(Sort.Direction.ASC, "data.company_name", "data.surname"))
                .and(Sort.by(Sort.Direction.ASC, "data.forename"))
                .and(Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before"));


        Sort actual = sortMapper.getSort(null);

        assertEquals(expected, actual);
    }

    @Test
    void testOfficerRoleSortByAppointedOn() throws Exception {

        Sort expected = Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before");

        Sort actual = sortMapper.getSort("appointed_on");

        assertEquals(expected, actual);
    }

    @Test
    void testOfficerRoleSortBySurname() throws Exception {

        Sort expected = Sort.by(Sort.Direction.ASC, "data.company_name", "data.surname");

        Sort actual = sortMapper.getSort("surname");

        assertEquals(expected, actual);
    }

    @Test
    void testOfficerRoleSortByResignedOn() throws Exception {

        Sort expected = Sort.by(Sort.Direction.DESC, "data.resigned_on");

        Sort actual = sortMapper.getSort("resigned_on");

        assertEquals(expected, actual);
    }

    @Test
    void testOfficerRoleSortByThrowsBadRequestExceptionWhenInvalidParameter() throws Exception {

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> sortMapper.getSort("invalid"));

        assertEquals("Invalid order by parameter [invalid]", thrown.getMessage());
    }
}
