package uk.gov.companieshouse.company_appointments.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.lang.Nullable;

/**
 * The type Delta identity verification details.
 */
public class DeltaIdentityVerificationDetails {

    @JsonProperty("anti_money_laundering_supervisory_bodies")
    @Field("anti_money_laundering_supervisory_bodies")
    @Nullable
    private List<String> antiMoneyLaunderingSupervisoryBodies;

    @JsonProperty("appointment_verification_end_on")
    @Field("appointment_verification_end_on")
    @Nullable
    private Instant appointmentVerificationEndOn;

    @JsonProperty("appointment_verification_statement_date")
    @Field("appointment_verification_statement_date")
    @Nullable
    private Instant appointmentVerificationStatementDate;

    @JsonProperty("appointment_verification_statement_due_on")
    @Field("appointment_verification_statement_due_on")
    @Nullable
    private Instant appointmentVerificationStatementDueOn;

    @JsonProperty("appointment_verification_start_on")
    @Field("appointment_verification_start_on")
    @Nullable
    private Instant appointmentVerificationStartOn;

    @JsonProperty("authorised_corporate_service_provider_name")
    @Field("authorised_corporate_service_provider_name")
    @Nullable
    private String authorisedCorporateServiceProviderName;

    @JsonProperty("identity_verified_on")
    @Field("identity_verified_on")
    @Nullable
    private Instant identityVerifiedOn;

    @JsonProperty("preferred_name")
    @Field("preferred_name")
    @Nullable
    private String preferredName;

    @Nullable
    public List<String> getAntiMoneyLaunderingSupervisoryBodies() {
        return antiMoneyLaunderingSupervisoryBodies;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setAntiMoneyLaunderingSupervisoryBodies(List<String> antiMoneyLaunderingSupervisoryBodies) {
        this.antiMoneyLaunderingSupervisoryBodies = antiMoneyLaunderingSupervisoryBodies;
        return this;
    }

    @Nullable
    public Instant getAppointmentVerificationEndOn() {
        return appointmentVerificationEndOn;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setAppointmentVerificationEndOn(Instant appointmentVerificationEndOn) {
        this.appointmentVerificationEndOn = appointmentVerificationEndOn;
        return this;
    }

    @Nullable
    public Instant getAppointmentVerificationStatementDate() {
        return appointmentVerificationStatementDate;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setAppointmentVerificationStatementDate(Instant appointmentVerificationStatementDate) {
        this.appointmentVerificationStatementDate = appointmentVerificationStatementDate;
        return this;
    }

    @Nullable
    public Instant getAppointmentVerificationStatementDueOn() {
        return appointmentVerificationStatementDueOn;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setAppointmentVerificationStatementDueOn(Instant appointmentVerificationStatementDueOn) {
        this.appointmentVerificationStatementDueOn = appointmentVerificationStatementDueOn;
        return this;
    }

    @Nullable
    public Instant getAppointmentVerificationStartOn() {
        return appointmentVerificationStartOn;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setAppointmentVerificationStartOn(Instant appointmentVerificationStartOn) {
        this.appointmentVerificationStartOn = appointmentVerificationStartOn;
        return this;
    }

    @Nullable
    public String getAuthorisedCorporateServiceProviderName() {
        return authorisedCorporateServiceProviderName;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setAuthorisedCorporateServiceProviderName(String authorisedCorporateServiceProviderName) {
        this.authorisedCorporateServiceProviderName = authorisedCorporateServiceProviderName;
        return this;
    }

    @Nullable
    public Instant getIdentityVerifiedOn() {
        return identityVerifiedOn;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setIdentityVerifiedOn(Instant identityVerifiedOn) {
        this.identityVerifiedOn = identityVerifiedOn;
        return this;
    }

    @Nullable
    public String getPreferredName() {
        return preferredName;
    }

    @Nullable
    public DeltaIdentityVerificationDetails setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        return this;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DeltaIdentityVerificationDetails that = (DeltaIdentityVerificationDetails) obj;
        return Objects.equals(
                this.antiMoneyLaunderingSupervisoryBodies, that.antiMoneyLaunderingSupervisoryBodies) &&
                Objects.equals(this.appointmentVerificationEndOn, that.appointmentVerificationEndOn) &&
                Objects.equals(
                        this.appointmentVerificationStatementDate, that.appointmentVerificationStatementDate) &&
                Objects.equals(
                        this.appointmentVerificationStatementDueOn, that.appointmentVerificationStatementDueOn) &&
                Objects.equals(this.appointmentVerificationStartOn, that.appointmentVerificationStartOn) &&
                Objects.equals(
                        this.authorisedCorporateServiceProviderName, that.authorisedCorporateServiceProviderName) &&
                Objects.equals(this.identityVerifiedOn, that.identityVerifiedOn) &&
                Objects.equals(this.preferredName, that.preferredName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(antiMoneyLaunderingSupervisoryBodies, appointmentVerificationEndOn,
                appointmentVerificationStatementDate, appointmentVerificationStatementDueOn,
                appointmentVerificationStartOn, authorisedCorporateServiceProviderName,
                identityVerifiedOn, preferredName);
    }
}
