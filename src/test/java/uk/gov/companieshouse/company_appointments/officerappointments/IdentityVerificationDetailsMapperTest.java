package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.officer.IdentityVerificationDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentityVerificationDetails;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationDetailsMapperTest {
    @InjectMocks
    private IdentityVerificationDetailsMapper mapper;

    @Test
    void mapIdentityVerificationDetails() {
        // given
        DeltaIdentityVerificationDetails deltaDetails = new DeltaIdentityVerificationDetails()
                .setAntiMoneyLaunderingSupervisoryBodies(List.of("Supervisor"))
                .setAppointmentVerificationEndOn(getInstant(1))
                .setAppointmentVerificationStatementDate(getInstant(2))
                .setAppointmentVerificationStatementDueOn(getInstant(3))
                .setAppointmentVerificationStartOn(getInstant(4))
                .setAuthorisedCorporateServiceProviderName("Provider")
                .setIdentityVerifiedOn(Instant.from(getInstant(5)))
                .setPreferredName("Preferred Name");

        IdentityVerificationDetails expected = new IdentityVerificationDetails()
                .antiMoneyLaunderingSupervisoryBodies(List.of("Supervisor"))
                .appointmentVerificationEndOn(getDateFromDay(1))
                .appointmentVerificationStatementDate(getDateFromDay(2))
                .appointmentVerificationStatementDueOn(getDateFromDay(3))
                .appointmentVerificationStartOn(getDateFromDay(4))
                .authorisedCorporateServiceProviderName("Provider")
                .identityVerifiedOn(getDateFromDay(5))
                .preferredName("Preferred Name");
        // when
        IdentityVerificationDetails actual = mapper.map(deltaDetails);
        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapIdentityVerificationDetailsEmpty() {
        // given
        DeltaIdentityVerificationDetails deltaDetails = new DeltaIdentityVerificationDetails();
        IdentityVerificationDetails expected = new IdentityVerificationDetails();
        // when
        IdentityVerificationDetails actual = mapper.map(deltaDetails);
        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapIdentityVerificationDetailsNull() {
        // when
        IdentityVerificationDetails actual = mapper.map(null);
        // then
        assertNull(actual);
    }

    private Instant getInstant(int date) {
        return Instant.from(getDateFromDay(date).atStartOfDay(UTC));
    }

    private LocalDate getDateFromDay(int day) {
        return LocalDate.of(2024, 1, day);
    }
}
