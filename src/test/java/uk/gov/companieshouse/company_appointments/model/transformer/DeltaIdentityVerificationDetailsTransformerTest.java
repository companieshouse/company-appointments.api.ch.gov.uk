package uk.gov.companieshouse.company_appointments.model.transformer;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

class DeltaIdentityVerificationDetailsTransformerTest {

    private final DeltaIdentityVerificationDetailsTransformer transformer =
            new DeltaIdentityVerificationDetailsTransformer();

    @Test
    void shouldTransformIdentityVerificationDetails() {
        IdentityVerificationDetails identityVerificationDetails = getIdentityVerificationDetails(List.of("Supervisory Body"));

        DeltaIdentityVerificationDetails result = transformer.transform(identityVerificationDetails);

        assertThat(result.getAntiMoneyLaunderingSupervisoryBodies()).isEqualTo(List.of("Supervisory Body"));
        assertThat(result.getAppointmentVerificationEndOn()).isEqualTo(getInstantForDayOfMonth(10));
        assertThat(result.getAppointmentVerificationStatementDate()).isEqualTo(getInstantForDayOfMonth(11));
        assertThat(result.getAppointmentVerificationStatementDueOn()).isEqualTo(getInstantForDayOfMonth(12));
        assertThat(result.getAppointmentVerificationStartOn()).isEqualTo(getInstantForDayOfMonth(13));
        assertThat(result.getAuthorisedCorporateServiceProviderName()).isEqualTo("Provider");
        assertThat(result.getIdentityVerifiedOn()).isEqualTo(getInstantForDayOfMonth(14));
        assertThat(result.getPreferredName()).isEqualTo("Preferred Name");
    }

    @Test
    void shouldTransformIdentityVerificationDetailsWithNullValue() {
        IdentityVerificationDetails identityVerificationDetails = getIdentityVerificationDetails(null);

        DeltaIdentityVerificationDetails result = transformer.transform(identityVerificationDetails);

        assertThat(result.getAntiMoneyLaunderingSupervisoryBodies()).isNull();
        assertThat(result.getAppointmentVerificationEndOn()).isEqualTo(getInstantForDayOfMonth(10));
        assertThat(result.getAppointmentVerificationStatementDate()).isEqualTo(getInstantForDayOfMonth(11));
        assertThat(result.getAppointmentVerificationStatementDueOn()).isEqualTo(getInstantForDayOfMonth(12));
        assertThat(result.getAppointmentVerificationStartOn()).isEqualTo(getInstantForDayOfMonth(13));
        assertThat(result.getAuthorisedCorporateServiceProviderName()).isEqualTo("Provider");
        assertThat(result.getIdentityVerifiedOn()).isEqualTo(getInstantForDayOfMonth(14));
        assertThat(result.getPreferredName()).isEqualTo("Preferred Name");
    }

    private static @NotNull IdentityVerificationDetails getIdentityVerificationDetails(List<String> supervisoryBodies) {
        IdentityVerificationDetails identityVerificationDetails = new IdentityVerificationDetails();
        identityVerificationDetails.setAntiMoneyLaunderingSupervisoryBodies(supervisoryBodies);
        identityVerificationDetails.setAppointmentVerificationEndOn(getLocalDateForDayOfMonth(10));
        identityVerificationDetails.setAppointmentVerificationStatementDate(getLocalDateForDayOfMonth(11));
        identityVerificationDetails.setAppointmentVerificationStatementDueOn(getLocalDateForDayOfMonth(12));
        identityVerificationDetails.setAppointmentVerificationStartOn(getLocalDateForDayOfMonth(13));
        identityVerificationDetails.setAuthorisedCorporateServiceProviderName("Provider");
        identityVerificationDetails.setIdentityVerifiedOn(getLocalDateForDayOfMonth(14));
        identityVerificationDetails.setPreferredName("Preferred Name");
        return identityVerificationDetails;
    }

    private static @NotNull Instant getInstantForDayOfMonth(int day) {
        return Instant.from(getLocalDateForDayOfMonth(day).atStartOfDay(UTC));
    }

    private static @NotNull LocalDate getLocalDateForDayOfMonth(int day) {
        return LocalDate.of(2024, 12, day);
    }
}
