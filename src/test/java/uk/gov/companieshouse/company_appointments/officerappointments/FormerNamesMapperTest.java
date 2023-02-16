package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.company_appointments.model.data.FormerNamesData;

class FormerNamesMapperTest {

    @Test
    void mapFormerNames() {
        // given
        List<FormerNamesData> formerNames = singletonList(new FormerNamesData("forenames", "surname"));

        List<FormerNames> expected = singletonList(new FormerNames()
                .forenames("forenames")
                .surname("surname"));
        // when
        List<FormerNames> actual = FormerNamesMapper.mapFormerNames(formerNames);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapFormerNamesNoNames() {
        // given
        List<FormerNamesData> formerNames = singletonList(new FormerNamesData());

        List<FormerNames> expected = singletonList(new FormerNames());
        // when
        List<FormerNames> actual = FormerNamesMapper.mapFormerNames(formerNames);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapFormerNamesEmpty() {
        // given
        List<FormerNamesData> formerNames = emptyList();

        List<FormerNames> expected = emptyList();
        // when
        List<FormerNames> actual = FormerNamesMapper.mapFormerNames(formerNames);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapFormerNamesNull() {
        // given
        // when
        List<FormerNames> actual = FormerNamesMapper.mapFormerNames(null);

        // then
        assertNull(actual);
    }
}