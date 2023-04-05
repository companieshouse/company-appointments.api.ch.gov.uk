package uk.gov.companieshouse.company_appointments.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyStatusValidatorTest {

    private CompanyStatusValidator companyStatusValidator = new CompanyStatusValidator();

    @ParameterizedTest
    @MethodSource("companyStatusFixtures")
    void testAcceptedCompanyStatusesPassesValidation(String companyStatus) {
        // when
        boolean result = companyStatusValidator.isValidCompanyStatus(companyStatus);

        // then
        assertTrue(result);
    }

    @Test
    void testInvalidStatusDoesNotPassValidation() {
        // when
        boolean result = companyStatusValidator.isValidCompanyStatus("fake");

        // then
        assertFalse(result);
    }

    private static Stream<Arguments> companyStatusFixtures() {
        return Stream.of(
            Arguments.of(Named.of("Test company status of [active] passes validation", "active")),
            Arguments.of(Named.of("Test company status of [dissolved] passes validation", "dissolved")),
            Arguments.of(Named.of("Test company status of [liquidation] passes validation", "liquidation")),
            Arguments.of(Named.of("Test company status of [receivership] passes validation", "receivership")),
            Arguments.of(Named.of("Test company status of [converted-closed] passes validation", "converted-closed")),
            Arguments.of(Named.of("Test company status of [open] passes validation", "open")),
            Arguments.of(Named.of("Test company status of [closed] passes validation", "closed")),
            Arguments.of(Named.of("Test company status of [insolvency-proceedings] passes validation", "insolvency-proceedings")),
            Arguments.of(Named.of("Test company status of [voluntary-arrangement] passes validation", "voluntary-arrangement")),
            Arguments.of(Named.of("Test company status of [administration] passes validation", "administration")),
            Arguments.of(Named.of("Test company status of [registered] passes validation", "registered")),
            Arguments.of(Named.of("Test company status of [removed] passes validation", "removed"))
        );
    }


}
