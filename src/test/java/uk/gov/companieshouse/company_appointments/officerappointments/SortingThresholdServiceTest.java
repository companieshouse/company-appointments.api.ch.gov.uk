package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SortingThresholdServiceTest {

    @ParameterizedTest
    @MethodSource("sortingThresholdScenarios")
    void testShouldSortByActiveThenResignedGivenTotalResultsAndThresholds(SortingThresholdTestArgument argument) {
        // given
        SortingThresholdService service = new SortingThresholdService(argument.internalSortingThreshold(),
                argument.externalSortingThreshold());

        // when
        boolean actual = service.shouldSortByActiveThenResigned(argument.totalResults(), argument.authPrivileges());

        // then
        assertEquals(argument.expected(), actual);
    }

    private static Stream<Arguments> sortingThresholdScenarios() {
        return Stream.of(
                Arguments.of(
                        Named.of("Should sort unauthorised request within external threshold",
                                SortingThresholdTestArgument.builder()
                                        .internalSortingThreshold(5)
                                        .externalSortingThreshold(25)
                                        .totalResults(10)
                                        .authPrivileges(null)
                                        .expected(true)
                                        .build())),
                Arguments.of(
                        Named.of("Should sort unauthorised request when external threshold -1",
                                SortingThresholdTestArgument.builder()
                                        .internalSortingThreshold(25)
                                        .externalSortingThreshold(-1)
                                        .totalResults(30)
                                        .authPrivileges(null)
                                        .expected(true)
                                        .build())),
                Arguments.of(
                        Named.of("Should not sort unauthorised request over external threshold",
                                SortingThresholdTestArgument.builder()
                                        .internalSortingThreshold(50)
                                        .externalSortingThreshold(25)
                                        .totalResults(30)
                                        .authPrivileges(null)
                                        .expected(false)
                                        .build())),
                Arguments.of(
                        Named.of("Should sort authorised request within internal threshold",
                                SortingThresholdTestArgument.builder()
                                        .internalSortingThreshold(25)
                                        .externalSortingThreshold(5)
                                        .totalResults(10)
                                        .authPrivileges("internal-app")
                                        .expected(true)
                                        .build())),
                Arguments.of(
                        Named.of("Should sort authorised request when internal threshold -1",
                                SortingThresholdTestArgument.builder()
                                        .internalSortingThreshold(-1)
                                        .externalSortingThreshold(25)
                                        .totalResults(30)
                                        .authPrivileges("internal-app")
                                        .expected(true)
                                        .build())),
                Arguments.of(
                        Named.of("Should not sort authorised request over internal threshold",
                                SortingThresholdTestArgument.builder()
                                        .internalSortingThreshold(25)
                                        .externalSortingThreshold(50)
                                        .totalResults(30)
                                        .authPrivileges("internal-app")
                                        .expected(false)
                                        .build())));
    }

    private record SortingThresholdTestArgument(int internalSortingThreshold, int externalSortingThreshold, int totalResults,
                                                String authPrivileges, boolean expected) {

        private static Builder builder() {
            return new Builder();
        }

        private static final class Builder {

            private int internalSortingThreshold;
            private int externalSortingThreshold;
            private int totalResults;
            private String authPrivileges;
            private boolean expected;

            private Builder() {
            }

            private Builder internalSortingThreshold(int internalSortingThreshold) {
                this.internalSortingThreshold = internalSortingThreshold;
                return this;
            }

            private Builder externalSortingThreshold(int externalSortingThreshold) {
                this.externalSortingThreshold = externalSortingThreshold;
                return this;
            }

            private Builder totalResults(int totalResults) {
                this.totalResults = totalResults;
                return this;
            }

            private Builder authPrivileges(String authPrivileges) {
                this.authPrivileges = authPrivileges;
                return this;
            }

            private Builder expected(boolean expected) {
                this.expected = expected;
                return this;
            }

            private SortingThresholdTestArgument build() {
                return new SortingThresholdTestArgument(internalSortingThreshold, externalSortingThreshold, totalResults,
                        authPrivileges, expected);
            }
        }
    }
}