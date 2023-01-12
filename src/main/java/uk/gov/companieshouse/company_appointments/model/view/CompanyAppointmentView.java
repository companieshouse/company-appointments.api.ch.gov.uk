package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyAppointmentView {

    @JsonProperty("address")
    private ServiceAddressView serviceAddress;

    @JsonProperty("appointed_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime appointedOn;

    @JsonProperty("appointed_before")
    private String appointedBefore;

    @JsonProperty("resigned_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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

    private String responsibilities;

    @JsonProperty("principal_office_address")
    private ServiceAddressView principalOfficeAddress;

    @JsonProperty("contact_details")
    private ContactDetailsView contactDetails;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("is_pre_1992_appointment")
    private Boolean isPre1992Appointment;

    public CompanyAppointmentView(
            ServiceAddressView serviceAddress, LocalDateTime appointedOn,
            String appointedBefore,
            LocalDateTime resignedOn, String countryOfResidence,
            DateOfBirth dateOfBirth,
            List<FormerNamesView> formerNames,
            IdentificationView identification,
            LinksView links, String name, String nationality, String occupation,
            String officerRole, String responsibilities,
            ServiceAddressView principalOfficeAddress,
            ContactDetailsView contactDetails, String etag,
            Boolean isPre1992Appointment) {
        this.serviceAddress = serviceAddress;
        this.appointedOn = appointedOn;
        this.appointedBefore = appointedBefore;
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
        this.responsibilities = responsibilities;
        this.principalOfficeAddress = principalOfficeAddress;
        this.contactDetails = contactDetails;
        this.etag = etag;
        this.isPre1992Appointment = isPre1992Appointment;
    }

    public CompanyAppointmentView() {}

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

    public String getAppointedBefore() {
        return appointedBefore;
    }

    public void setAppointedBefore(String appointedBefore) {
        this.appointedBefore = appointedBefore;
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

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public ServiceAddressView getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(
            ServiceAddressView principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public ContactDetailsView getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(
            ContactDetailsView contactDetails) {
        this.contactDetails = contactDetails;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Boolean getIsPre1992Appointment() {
        return isPre1992Appointment;
    }

    public void setIsPre1992Appointment(Boolean isPre1992Appointment) {
        isPre1992Appointment = isPre1992Appointment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ServiceAddressView serviceAddress;
        private LocalDateTime appointedOn;
        private String appointedBefore;
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
        private String responsibilities;
        private ServiceAddressView principalOfficeAddress;
        private ContactDetailsView contactDetails;
        private String etag;
        private Boolean isPre1992Appointment;

        public Builder withServiceAddress(ServiceAddressView serviceAddress) {
            this.serviceAddress = serviceAddress;
            return this;
        }

        public Builder withAppointedOn(LocalDateTime appointedOn) {
            this.appointedOn = appointedOn;
            return this;
        }

        public Builder withAppointedBefore(String appointedBefore) {
            this.appointedBefore = appointedBefore;
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

        public Builder withResponsibilities(String responsibilities) {
            this.responsibilities = responsibilities;
            return this;
        }

        public Builder withPrincipalOfficeAddress(ServiceAddressView principalOfficeAddress) {
            this.principalOfficeAddress = principalOfficeAddress;
            return this;
        }

        public Builder withContactDetails(ContactDetailsView contactDetails) {
            this.contactDetails = contactDetails;
            return this;
        }

        public Builder withEtag(String etag) {
            this.etag = etag;
            return this;
        }

        public Builder withIsPre1992Appointment(Boolean isPre1992Appointment) {
            this.isPre1992Appointment = isPre1992Appointment;
            return this;
        }

        public CompanyAppointmentView build() {
            return new CompanyAppointmentView(serviceAddress, appointedOn, appointedBefore, resignedOn, countryOfResidence, dateOfBirth,
                    formerNames, identification, links, name, nationality, occupation, officerRole, responsibilities,
                    principalOfficeAddress, contactDetails, etag, isPre1992Appointment);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyAppointmentView)) return false;
        CompanyAppointmentView that = (CompanyAppointmentView) o;
        return Objects.equals(getServiceAddress(), that.getServiceAddress()) &&
                Objects.equals(getAppointedOn(), that.getAppointedOn()) &&
                Objects.equals(getAppointedBefore(), that.getAppointedBefore()) &&
                Objects.equals(getResignedOn(), that.getResignedOn()) &&
                Objects.equals(getCountryOfResidence(), that.getCountryOfResidence()) &&
                Objects.equals(getDateOfBirth(), that.getDateOfBirth()) &&
                Objects.equals(getFormerNames(), that.getFormerNames()) &&
                Objects.equals(getIdentification(), that.getIdentification()) &&
                Objects.equals(getLinks(), that.getLinks()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getNationality(), that.getNationality()) &&
                Objects.equals(getOccupation(), that.getOccupation()) &&
                Objects.equals(getOfficerRole(), that.getOfficerRole()) &&
                Objects.equals(getResponsibilities(), that.getResponsibilities()) &&
                Objects.equals(getPrincipalOfficeAddress(), that.getPrincipalOfficeAddress()) &&
                Objects.equals(getContactDetails(), that.getContactDetails()) &&
                Objects.equals(getEtag(), that.getEtag()) &&
                Objects.equals(getIsPre1992Appointment(), that.getIsPre1992Appointment());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getServiceAddress(), getAppointedOn(), getAppointedBefore(), getResignedOn(),
                        getCountryOfResidence(), getDateOfBirth(), getFormerNames(), getIdentification(),
                        getLinks(), getName(), getNationality(), getOccupation(), getOfficerRole(),
                        getResponsibilities(), getPrincipalOfficeAddress(), getContactDetails(), getEtag(),
                        getIsPre1992Appointment());
    }
}
