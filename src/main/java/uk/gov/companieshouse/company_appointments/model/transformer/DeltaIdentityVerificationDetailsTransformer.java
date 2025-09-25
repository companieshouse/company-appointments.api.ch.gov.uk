package uk.gov.companieshouse.company_appointments.model.transformer;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

@Component
public class DeltaIdentityVerificationDetailsTransformer implements Transformative<IdentityVerificationDetails, DeltaIdentityVerificationDetails> {
    @Override
    public DeltaIdentityVerificationDetails factory() {
        return new DeltaIdentityVerificationDetails();
    }

    @Override
    public DeltaIdentityVerificationDetails transform(IdentityVerificationDetails source, DeltaIdentityVerificationDetails output)
            throws FailedToTransformException {
        try {
            Optional<LocalDate> appointmentVerificationEndOn = Optional.ofNullable(source.getAppointmentVerificationEndOn());
            Optional<LocalDate> appointmentVerificationStatementDate = Optional.ofNullable(source.getAppointmentVerificationStatementDate());
            Optional<LocalDate> appointmentVerificationStatementDueOn = Optional.ofNullable(source.getAppointmentVerificationStatementDueOn());
            Optional<LocalDate> appointmentVerificationStartOn = Optional.ofNullable(source.getAppointmentVerificationStartOn());
            Optional<LocalDate> identityVerifiedOn = Optional.ofNullable(source.getIdentityVerifiedOn());

            appointmentVerificationEndOn.ifPresent(instant -> output.setAppointmentVerificationEndOn(Instant.from(instant.atStartOfDay(UTC))));
            appointmentVerificationStatementDate.ifPresent(instant -> output.setAppointmentVerificationStatementDate(Instant.from(instant.atStartOfDay(UTC))));
            appointmentVerificationStatementDueOn.ifPresent(instant -> output.setAppointmentVerificationStatementDueOn(Instant.from(instant.atStartOfDay(UTC))));
            appointmentVerificationStartOn.ifPresent(instant -> output.setAppointmentVerificationStartOn(Instant.from(instant.atStartOfDay(UTC))));
            identityVerifiedOn.ifPresent(instant -> output.setIdentityVerifiedOn(Instant.from(instant.atStartOfDay(UTC))));

            output.setAuthorisedCorporateServiceProviderName(source.getAuthorisedCorporateServiceProviderName());
            output.setAntiMoneyLaunderingSupervisoryBodies(source.getAntiMoneyLaunderingSupervisoryBodies());
            output.setPreferredName(source.getPreferredName());

            return output;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform IdentityVerificationDetails: %s", e.getMessage()));
        }
    }
}
