package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

import java.time.Instant;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;

@Component
class IdentityVerificationDetailsMapper {

    IdentityVerificationDetails map(DeltaIdentityVerificationDetails deltaDetails) {
        return ofNullable(deltaDetails)
                .map(details -> new IdentityVerificationDetails()
                        .antiMoneyLaunderingSupervisoryBodies(details.getAntiMoneyLaunderingSupervisoryBodies())
                        .appointmentVerificationEndOn(getLocalDate(details.getAppointmentVerificationEndOn()))
                        .appointmentVerificationStatementDate(getLocalDate(details.getAppointmentVerificationStatementDate()))
                        .appointmentVerificationStatementDueOn(getLocalDate(details.getAppointmentVerificationStatementDueOn()))
                        .appointmentVerificationStartOn(getLocalDate(details.getAppointmentVerificationStartOn()))
                        .authorisedCorporateServiceProviderName(details.getAuthorisedCorporateServiceProviderName())
                        .identityVerifiedOn(getLocalDate(details.getIdentityVerifiedOn()))
                        .preferredName(details.getPreferredName())
                ).orElse(null);
    }

    private LocalDate getLocalDate(Instant date) {
        return date != null ? LocalDate.from(date.atZone(UTC)) : null;
    }
}
