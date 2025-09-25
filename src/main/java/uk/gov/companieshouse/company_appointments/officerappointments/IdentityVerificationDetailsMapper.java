package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

@Component
class IdentityVerificationDetailsMapper {

    IdentityVerificationDetails map(DeltaIdentityVerificationDetails deltaDetails) {
        if (deltaDetails == null) return null;

        IdentityVerificationDetails ivd = new IdentityVerificationDetails();

        Optional<Instant> appointmentVerificationEndOn = Optional.ofNullable(deltaDetails.getAppointmentVerificationEndOn());
        Optional<Instant> appointmentVerificationStatementDate = Optional.ofNullable(deltaDetails.getAppointmentVerificationStatementDate());
        Optional<Instant> appointmentVerificationStatementDueOn = Optional.ofNullable(deltaDetails.getAppointmentVerificationStatementDueOn());
        Optional<Instant> appointmentVerificationStartOn = Optional.ofNullable(deltaDetails.getAppointmentVerificationStartOn());
        Optional<Instant> identityVerifiedOn = Optional.ofNullable(deltaDetails.getIdentityVerifiedOn());

        appointmentVerificationEndOn.ifPresent(instant -> ivd.setAppointmentVerificationEndOn(LocalDate.from(instant.atZone(UTC))));
        appointmentVerificationStatementDate.ifPresent(instant -> ivd.setAppointmentVerificationStatementDate(LocalDate.from(instant.atZone(UTC))));
        appointmentVerificationStatementDueOn.ifPresent(instant -> ivd.setAppointmentVerificationStatementDueOn(LocalDate.from(instant.atZone(UTC))));
        appointmentVerificationStartOn.ifPresent(instant -> ivd.appointmentVerificationStartOn(LocalDate.from(instant.atZone(UTC))));
        identityVerifiedOn.ifPresent(instant -> ivd.identityVerifiedOn(LocalDate.from(instant.atZone(UTC))));

        ivd.setAuthorisedCorporateServiceProviderName(deltaDetails.getAuthorisedCorporateServiceProviderName());
        ivd.setAntiMoneyLaunderingSupervisoryBodies(deltaDetails.getAntiMoneyLaunderingSupervisoryBodies());
        ivd.setPreferredName(deltaDetails.getPreferredName());

        return ivd;
    }
}
