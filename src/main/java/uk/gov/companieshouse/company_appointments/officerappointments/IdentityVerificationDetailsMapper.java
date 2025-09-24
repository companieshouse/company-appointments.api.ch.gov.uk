package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

@Component
class IdentityVerificationDetailsMapper {

    IdentityVerificationDetails map(DeltaIdentityVerificationDetails deltaDetails) {
        if (deltaDetails == null) return null;

        IdentityVerificationDetails ivd = new IdentityVerificationDetails();

        setIfNotNull(ivd::setAntiMoneyLaunderingSupervisoryBodies, deltaDetails.getAntiMoneyLaunderingSupervisoryBodies());
        setIfNotNull(ivd::setAppointmentVerificationEndOn, getLocalDate(deltaDetails.getAppointmentVerificationEndOn()));
        setIfNotNull(ivd::setAppointmentVerificationStatementDate, getLocalDate(deltaDetails.getAppointmentVerificationStatementDate()));
        setIfNotNull(ivd::setAppointmentVerificationStatementDueOn, getLocalDate(deltaDetails.getAppointmentVerificationStatementDueOn()));
        setIfNotNull(ivd::setAppointmentVerificationStartOn, getLocalDate(deltaDetails.getAppointmentVerificationStartOn()));
        setIfNotNull(ivd::setAuthorisedCorporateServiceProviderName, deltaDetails.getAuthorisedCorporateServiceProviderName());
        setIfNotNull(ivd::setIdentityVerifiedOn, getLocalDate(deltaDetails.getIdentityVerifiedOn()));
        setIfNotNull(ivd::setPreferredName, deltaDetails.getPreferredName());

        return ivd;
    }

    private LocalDate getLocalDate(Instant date) {
        return date != null ? LocalDate.from(date.atZone(UTC)) : null;
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
