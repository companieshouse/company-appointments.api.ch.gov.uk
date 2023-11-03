package uk.gov.companieshouse.company_appointments.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.data.CompanyStatus;

@ExtendWith(MockitoExtension.class)
class CompanyStatusValidatorTest {

    private final CompanyStatusValidator companyStatusValidator = new CompanyStatusValidator();

    @ParameterizedTest
    @EnumSource(CompanyStatus.class)
    void testAcceptedCompanyStatusesPassesValidation(CompanyStatus status) {
        // when
        boolean result = companyStatusValidator.isValidCompanyStatus(status.getStatus());

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
}
