package uk.gov.companieshouse.company_appointments.officerappointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.ContributionSubType;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContributionSubType;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContributionSubTypesMapperTest {

    private ContributionSubTypesMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ContributionSubTypesMapper();
    }

    @Test
    void mapContributionSubTypes() {
        // given
        List<DeltaContributionSubType> subTypes = singletonList(new DeltaContributionSubType("money"));
        List<ContributionSubType> expected = singletonList(new ContributionSubType().subType("money"));

        // when
        List<ContributionSubType> actual = mapper.map(subTypes);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapContributionSubTypesWhenNonePresent() {
        // given
        List<DeltaContributionSubType> subTypes = singletonList(new DeltaContributionSubType());
        List<ContributionSubType> expected = singletonList(new ContributionSubType());

        // when
        List<ContributionSubType> actual = mapper.map(subTypes);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapContributionSubTypesWhenListIsEmpty() {
        // given
        List<DeltaContributionSubType> subTypes = emptyList();
        List<ContributionSubType> expected = emptyList();

        // when
        List<ContributionSubType> actual = mapper.map(subTypes);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapContributionSubTypesWithNullValue() {
        // given

        // when
        List<ContributionSubType> actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}