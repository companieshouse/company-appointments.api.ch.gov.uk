package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

import java.time.Instant;

import static java.time.ZoneOffset.UTC;

@Component
public class DeltaIdentityVerificationDetailsTransformer implements Transformative<IdentityVerificationDetails, DeltaIdentityVerificationDetails> {
    @Override
    public DeltaIdentityVerificationDetails factory() {
        return new DeltaIdentityVerificationDetails();
    }

    @Override
    public DeltaIdentityVerificationDetails transform(IdentityVerificationDetails source, DeltaIdentityVerificationDetails output) throws FailedToTransformException {
        try {
            return output
                    .setAntiMoneyLaunderingSupervisoryBodies(source.getAntiMoneyLaunderingSupervisoryBodies() != null ?
                    source.getAntiMoneyLaunderingSupervisoryBodies() : null)
                    .setAppointmentVerificationEndOn(
                            Instant.from(source.getAppointmentVerificationEndOn().atStartOfDay(UTC)))
                    .setAppointmentVerificationStatementDate(
                            Instant.from(source.getAppointmentVerificationStatementDate().atStartOfDay(UTC)))
                    .setAppointmentVerificationStatementDueOn(
                            Instant.from(source.getAppointmentVerificationStatementDueOn().atStartOfDay(UTC)))
                    .setAppointmentVerificationStartOn(
                            Instant.from(source.getAppointmentVerificationStartOn().atStartOfDay(UTC)))
                    .setAuthorisedCorporateServiceProviderName(source.getAuthorisedCorporateServiceProviderName())
                    .setIdentityVerifiedOn(Instant.from(source.getIdentityVerifiedOn().atStartOfDay(UTC)))
                    .setPreferredName(source.getPreferredName());
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform IdentityVerificationDetails: %s", e.getMessage()));
        }
    }
}
