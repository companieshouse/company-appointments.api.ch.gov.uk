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
            output.setAntiMoneyLaunderingSupervisoryBodies(source.getAntiMoneyLaunderingSupervisoryBodies() != null ?
                    source.getAntiMoneyLaunderingSupervisoryBodies() : null);
            output.setAppointmentVerificationEndOn(Instant.from(source.getAppointmentVerificationEndOn().atStartOfDay(UTC)));
            output.setAppointmentVerificationStatementDate(Instant.from(source.getAppointmentVerificationStatementDate().atStartOfDay(UTC)));
            output.setAppointmentVerificationStatementDueOn(Instant.from(source.getAppointmentVerificationStatementDueOn().atStartOfDay(UTC)));
            output.setAppointmentVerificationStartOn(Instant.from(source.getAppointmentVerificationStartOn().atStartOfDay(UTC)));
            output.setAuthorisedCorporateServiceProviderName(source.getAuthorisedCorporateServiceProviderName());
            output.setIdentityVerifiedOn(Instant.from(source.getIdentityVerifiedOn().atStartOfDay(UTC)));
            output.setPreferredName(source.getPreferredName());
            return output;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform IdentityVerificationDetails: %s", e.getMessage()));
        }
    }
}
