package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ItemsPerPageServiceTest {

    private ItemsPerPageService service;

    @BeforeEach
    void setUp() {
        service = new ItemsPerPageService(500);
    }

    @ParameterizedTest
    @MethodSource("itemsPerPageScenarios")
    void testAdjustItemsPerPage(ItemsPerPageTestArgument argument) {
        // given

        // when
        int actual = service.adjustItemsPerPage(argument.getItemsPerPage(), argument.getAuthPrivileges());

        // then
        assertEquals(argument.getExpectedItemsPerPage(), actual);
    }

    private static Stream<Arguments> itemsPerPageScenarios() {
        return Stream.of(
                Arguments.of(
                        Named.of("Null items per page returns default 35 items per page",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(null)
                                        .authPrivileges(null)
                                        .expectedItemsPerPage(35)
                                        .build())),
                Arguments.of(
                        Named.of("Zero items per page returns default 35 items per page",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(0)
                                        .authPrivileges(null)
                                        .expectedItemsPerPage(35)
                                        .build())),
                Arguments.of(
                        Named.of("20 items per page returns 20 items per page",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(20)
                                        .authPrivileges(null)
                                        .expectedItemsPerPage(20)
                                        .build())),
                Arguments.of(
                        Named.of("51 items per page returns 50 items per page when no internal-app privileges",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(51)
                                        .authPrivileges(null)
                                        .expectedItemsPerPage(50)
                                        .build())),
                Arguments.of(
                        Named.of("500 items per page returns 500 items per page when internal-app privileges",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(500)
                                        .authPrivileges("any,internal-app,any")
                                        .expectedItemsPerPage(500)
                                        .build())),
                Arguments.of(
                        Named.of("490 items per page returns 490 items per page when internal-app privileges",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(490)
                                        .authPrivileges("any,internal-app,any")
                                        .expectedItemsPerPage(490)
                                        .build())),
                Arguments.of(
                        Named.of("501 items per page returns 500 items per page when internal-app privileges",
                                ItemsPerPageTestArgument.Builder.builder()
                                        .itemsPerPage(501)
                                        .authPrivileges("any,internal-app,any")
                                        .expectedItemsPerPage(500)
                                        .build())));
    }

    private static class ItemsPerPageTestArgument {
        private final Integer itemsPerPage;
        private final String authPrivileges;
        private final int expectedItemsPerPage;

        private ItemsPerPageTestArgument(Builder builder) {
            itemsPerPage = builder.itemsPerPage;
            authPrivileges = builder.authPrivileges;
            expectedItemsPerPage = builder.expectedItemsPerPage;
        }

        public Integer getItemsPerPage() {
            return itemsPerPage;
        }

        public String getAuthPrivileges() {
            return authPrivileges;
        }

        public int getExpectedItemsPerPage() {
            return expectedItemsPerPage;
        }

        public static final class Builder {
            private Integer itemsPerPage;
            private String authPrivileges;
            private int expectedItemsPerPage;

            private Builder() {
            }

            public static Builder builder() {
                return new Builder();
            }

            public Builder itemsPerPage(Integer itemsPerPage) {
                this.itemsPerPage = itemsPerPage;
                return this;
            }

            public Builder authPrivileges(String authPrivileges) {
                this.authPrivileges = authPrivileges;
                return this;
            }

            public Builder expectedItemsPerPage(int expectedItemsPerPage) {
                this.expectedItemsPerPage = expectedItemsPerPage;
                return this;
            }

            public ItemsPerPageTestArgument build() {
                return new ItemsPerPageTestArgument(this);
            }
        }
    }
}