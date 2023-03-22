package uk.gov.companieshouse.company_appointments.model.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class OfficerData {

    @Field("service_address")
    private ServiceAddressData serviceAddress;

    @Field("appointed_on")
    private LocalDateTime appointedOn;

    @Field("appointed_before")
    private String appointedBefore;

    @Field("resigned_on")
    private LocalDateTime resignedOn;

    @Field("country_of_residence")
    private String countryOfResidence;

    @Field("links")
    private LinksData linksData;

    private String nationality;

    private String occupation;

    @Field("officer_role")
    private String officerRole;

    @Field("date_of_birth")
    private LocalDateTime dateOfBirth;

    @Field("identification")
    private IdentificationData identificationData;

    @Field("former_names")
    private List<FormerNamesData> formerNameData;

    private String surname;

    private String forename;

    @Field("other_forenames")
    private String otherForenames;

    private String title;

    private String honours;

    @Field("company_name")
    private String companyName;

    @Field("company_number")
    private String companyNumber;

    private String responsibilities;

    @Field("principal_office_address")
    private ServiceAddressData principalOfficeAddress;

    @Field("contact_details")
    private ContactDetailsData contactDetails;

    private String etag;

    @Field("is_pre_1992_appointment")
    private Boolean isPre1992Appointment;

    public OfficerData(
            ServiceAddressData serviceAddress, LocalDateTime appointedOn, String appointedBefore,
            LocalDateTime resignedOn, String countryOfResidence,
            LinksData linksData, String nationality, String occupation, String officerRole,
            LocalDateTime dateOfBirth,
            IdentificationData identificationData,
            List<FormerNamesData> formerNameData, String surname, String forename,
            String otherForenames, String title, String honours, String companyName, String companyNumber,
            String responsibilities, ServiceAddressData principalOfficeAddress,
            ContactDetailsData contactDetails, String etag, Boolean isPre1992Appointment) {
        this.serviceAddress = serviceAddress;
        this.appointedOn = appointedOn;
        this.appointedBefore = appointedBefore;
        this.resignedOn = resignedOn;
        this.countryOfResidence = countryOfResidence;
        this.linksData = linksData;
        this.nationality = nationality;
        this.occupation = occupation;
        this.officerRole = officerRole;
        this.dateOfBirth = dateOfBirth;
        this.identificationData = identificationData;
        this.formerNameData = formerNameData;
        this.surname = surname;
        this.forename = forename;
        this.otherForenames = otherForenames;
        this.title = title;
        this.honours = honours;
        this.companyName = companyName;
        this.companyNumber = companyNumber;
        this.responsibilities = responsibilities;
        this.principalOfficeAddress = principalOfficeAddress;
        this.contactDetails = contactDetails;
        this.etag = etag;
        this.isPre1992Appointment = isPre1992Appointment;
    }

    public ServiceAddressData getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(ServiceAddressData serviceAddress) {
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

    public LinksData getLinksData() {
        return linksData;
    }

    public void setLinksData(LinksData linksData) {
        this.linksData = linksData;
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

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public IdentificationData getIdentificationData() {
        return identificationData;
    }

    public void setIdentificationData(IdentificationData identificationData) {
        this.identificationData = identificationData;
    }

    public List<FormerNamesData> getFormerNameData() {
        return formerNameData;
    }

    public void setFormerNameData(List<FormerNamesData> formerNameData) {
        this.formerNameData = formerNameData;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getOtherForenames() {
        return otherForenames;
    }

    public void setOtherForenames(String otherForenames) {
        this.otherForenames = otherForenames;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHonours() {
        return honours;
    }

    public OfficerData setHonours(String honours) {
        this.honours = honours;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public OfficerData setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public ServiceAddressData getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(ServiceAddressData principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public ContactDetailsData getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetailsData contactDetails) {
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
        this.isPre1992Appointment = isPre1992Appointment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServiceAddressData serviceAddress;
        private LocalDateTime appointedOn;
        private String appointedBefore;
        private LocalDateTime resignedOn;
        private String countryOfResidence;
        private LinksData linksData;
        private String nationality;
        private String occupation;
        private String officerRole;
        private LocalDateTime dateOfBirth;
        private IdentificationData identificationData;
        private List<FormerNamesData> formerNameData;
        private String surname;
        private String forename;
        private String otherForenames;
        private String title;
        private String honours;
        private String companyName;
        private String companyNumber;
        private String responsibilities;
        private ServiceAddressData principalOfficeAddress;
        private ContactDetailsData contactDetailsData;
        private String etag;
        private Boolean isPre1992Appointment;

        public Builder withServiceAddress(ServiceAddressData serviceAddress) {
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

        public Builder withResignedOn(LocalDateTime resignedOn) {
            this.resignedOn = resignedOn;
            return this;
        }

        public Builder withCountryOfResidence(String countryOfResidence) {
            this.countryOfResidence = countryOfResidence;
            return this;
        }

        public Builder withLinks(LinksData linksData) {
            this.linksData = linksData;
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

        public Builder withDateOfBirth(LocalDateTime dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withIdentification(IdentificationData identificationData) {
            this.identificationData = identificationData;
            return this;
        }

        public Builder withFormerNames(List<FormerNamesData> formerNameData) {
            this.formerNameData = formerNameData;
            return this;
        }

        public Builder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder withForename(String forename) {
            this.forename = forename;
            return this;
        }

        public Builder withOtherForenames(String otherForenames) {
            this.otherForenames = otherForenames;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withHonours(String honours) {
            this.honours = honours;
            return this;
        }

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withResponsibilities(String responsibilities) {
            this.responsibilities = responsibilities;
            return this;
        }

        public Builder withPrincipalOfficeAddress(ServiceAddressData principalOfficeAddress) {
            this.principalOfficeAddress = principalOfficeAddress;
            return this;
        }

        public Builder withContactDetails(ContactDetailsData contactDetailsData) {
            this.contactDetailsData = contactDetailsData;
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

        public OfficerData build() {
            return new OfficerData(serviceAddress, appointedOn, appointedBefore, resignedOn, countryOfResidence, linksData, nationality,
                    occupation, officerRole, dateOfBirth, identificationData, formerNameData, surname, forename,
                    otherForenames, title, honours, companyName, companyNumber, responsibilities, principalOfficeAddress,
                    contactDetailsData, etag, isPre1992Appointment);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OfficerData that = (OfficerData) o;
        return Objects.equals(serviceAddress, that.serviceAddress)
                && Objects.equals(appointedOn, that.appointedOn)
                && Objects.equals(appointedBefore, that.appointedBefore)
                && Objects.equals(resignedOn, that.resignedOn)
                && Objects.equals(countryOfResidence, that.countryOfResidence)
                && Objects.equals(linksData, that.linksData)
                && Objects.equals(nationality, that.nationality)
                && Objects.equals(occupation, that.occupation)
                && Objects.equals(officerRole, that.officerRole)
                && Objects.equals(dateOfBirth, that.dateOfBirth)
                && Objects.equals(identificationData, that.identificationData)
                && Objects.equals(formerNameData, that.formerNameData)
                && Objects.equals(surname, that.surname)
                && Objects.equals(forename, that.forename)
                && Objects.equals(otherForenames, that.otherForenames)
                && Objects.equals(title, that.title)
                && Objects.equals(honours, that.honours)
                && Objects.equals(companyName, that.companyName)
                && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(responsibilities, that.responsibilities)
                && Objects.equals(principalOfficeAddress, that.principalOfficeAddress)
                && Objects.equals(contactDetails, that.contactDetails)
                && Objects.equals(etag, that.etag)
                && Objects.equals(isPre1992Appointment, that.isPre1992Appointment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceAddress, appointedOn, appointedBefore, resignedOn, countryOfResidence, linksData, nationality, occupation, officerRole, dateOfBirth, identificationData,
                formerNameData,
                surname, forename, otherForenames, title, honours, companyName, companyNumber, responsibilities, principalOfficeAddress, contactDetails, etag, isPre1992Appointment);
    }
}
