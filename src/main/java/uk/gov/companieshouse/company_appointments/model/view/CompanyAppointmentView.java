package uk.gov.companieshouse.company_appointments.model.view;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyAppointmentView {

    @JsonProperty("address")
    private ServiceAddressView serviceAddress;

    @JsonProperty("appointed_on")
    private LocalDateTime appointedOn;

    @JsonProperty("resigned_on")
    private LocalDateTime resignedOn;

    @JsonProperty("country_of_residence")
    private String countryOfResidence;

    @JsonProperty("date_of_birth")
    private DateOfBirth dateOfBirth;

    @JsonProperty("former_names")
    private List<FormerNamesView> formerNames;

    private IdentificationView identification;

    private LinksView links;

    private String name;

    private String nationality;

    private String occupation;

    @JsonProperty("officer_role")
    private String officerRole;

    public CompanyAppointmentView(ServiceAddressView serviceAddress, LocalDateTime appointedOn,
            LocalDateTime resignedOn, String countryOfResidence, DateOfBirth dateOfBirth,
            List<FormerNamesView> formerNames, IdentificationView identification, LinksView links, String name,
            String nationality, String occupation, String officerRole) {
        this.serviceAddress = serviceAddress;
        this.appointedOn = appointedOn;
        this.resignedOn = resignedOn;
        this.countryOfResidence = countryOfResidence;
        this.dateOfBirth = dateOfBirth;
        this.formerNames = formerNames;
        this.identification = identification;
        this.links = links;
        this.name = name;
        this.nationality = nationality;
        this.occupation = occupation;
        this.officerRole = officerRole;
        this.resignedOn = resignedOn;
    }

    public ServiceAddressView getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(ServiceAddressView serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public LocalDateTime getAppointedOn() {
        return appointedOn;
    }

    public void setAppointedOn(LocalDateTime appointedOn) {
        this.appointedOn = appointedOn;
    }

    public LocalDateTime getResignedOn() {
        return resignedOn;
    }

    public void setResignedOn(LocalDateTime resignedOn) {
        this.resignedOn = resignedOn;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public void setCountryOfResidence(String countryOfResidence) {
        this.countryOfResidence = countryOfResidence;
    }

    public DateOfBirth getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateOfBirth dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<FormerNamesView> getFormerNames() {
        return formerNames;
    }

    public void setFormerNames(List<FormerNamesView> formerNames) {
        this.formerNames = formerNames;
    }

    public IdentificationView getIdentification() {
        return identification;
    }

    public void setIdentification(IdentificationView identification) {
        this.identification = identification;
    }

    public LinksView getLinks() {
        return links;
    }

    public void setLinks(LinksView links) {
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getOfficerRole() {
        return officerRole;
    }

    public void setOfficerRole(String officerRole) {
        this.officerRole = officerRole;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {

        private ServiceAddressView serviceAddress;
        private LocalDateTime appointedOn;
        private LocalDateTime resignedOn;
        private String countryOfResidence;
        private DateOfBirth dateOfBirth;
        private List<FormerNamesView> formerNames;
        private IdentificationView identification;
        private LinksView links;
        private String name;
        private String nationality;
        private String occupation;
        private String officerRole;

        public Builder withServiceAddress(ServiceAddressView serviceAddress) {
            this.serviceAddress = serviceAddress;
            return this;
        }

        public Builder withAppointedOn(LocalDateTime appointedOn) {
            this.appointedOn = appointedOn;
            return this;
        }

        public Builder withCountryOfResidence(String countryOfResidence) {
            this.countryOfResidence = countryOfResidence;
            return this;
        }

        public Builder withDateOfBirth(DateOfBirth dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withFormerNames(List<FormerNamesView> formerNames) {
            this.formerNames = formerNames;
            return this;
        }

        public Builder withIdentification(IdentificationView identification) {
            this.identification = identification;
            return this;
        }

        public Builder withLinks(LinksView links) {
            this.links = links;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withNationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withOccupation(String occupation) {
            this.occupation = occupation;
            return this;
        }

        public Builder withOfficerRole(String officerRole) {
            this.officerRole = officerRole;
            return this;
        }

        public Builder withResignedOn(LocalDateTime resignedOn) {
            this.resignedOn = resignedOn;
            return this;
        }

        public CompanyAppointmentView build() {
            return new CompanyAppointmentView(serviceAddress, appointedOn, resignedOn, countryOfResidence, dateOfBirth,
                    formerNames, identification, links, name, nationality, occupation, officerRole);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyAppointmentView)) return false;
        CompanyAppointmentView that = (CompanyAppointmentView) o;
        return Objects.equals(getServiceAddress(), that.getServiceAddress()) &&
                Objects.equals(getAppointedOn(), that.getAppointedOn()) &&
                Objects.equals(getResignedOn(), that.getResignedOn()) &&
                Objects.equals(getCountryOfResidence(), that.getCountryOfResidence()) &&
                Objects.equals(getDateOfBirth(), that.getDateOfBirth()) &&
                Objects.equals(getFormerNames(), that.getFormerNames()) &&
                Objects.equals(getIdentification(), that.getIdentification()) &&
                Objects.equals(getLinks(), that.getLinks()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getNationality(), that.getNationality()) &&
                Objects.equals(getOccupation(), that.getOccupation()) &&
                Objects.equals(getOfficerRole(), that.getOfficerRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceAddress(), getAppointedOn(), getResignedOn(), getCountryOfResidence(),
                getDateOfBirth(), getFormerNames(), getIdentification(), getLinks(), getName(), getNationality(),
                getOccupation(), getOfficerRole());
    }

}
