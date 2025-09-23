package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;

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
            setIfNotNull(output::setAntiMoneyLaunderingSupervisoryBodies, source.getAntiMoneyLaunderingSupervisoryBodies());
            setIfNotNull(output::setAppointmentVerificationEndOn, toInstant(source.getAppointmentVerificationEndOn()));
            setIfNotNull(output::setAppointmentVerificationStatementDate, toInstant(source.getAppointmentVerificationStatementDate()));
            setIfNotNull(output::setAppointmentVerificationStatementDueOn, toInstant(source.getAppointmentVerificationStatementDueOn()));
            setIfNotNull(output::setAppointmentVerificationStartOn, toInstant(source.getAppointmentVerificationStartOn()));
            setIfNotNull(output::setAuthorisedCorporateServiceProviderName, source.getAuthorisedCorporateServiceProviderName());
            setIfNotNull(output::setIdentityVerifiedOn, toInstant(source.getIdentityVerifiedOn()));
            setIfNotNull(output::setPreferredName, source.getPreferredName());

            return output;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform IdentityVerificationDetails: %s", e.getMessage()));
        }
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private Instant toInstant(LocalDate date) {
        return date != null ? Instant.from(date.atStartOfDay(UTC)) : null;
    }
}
