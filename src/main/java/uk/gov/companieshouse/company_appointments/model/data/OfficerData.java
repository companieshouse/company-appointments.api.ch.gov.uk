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

    @Field("company_name")
    private String companyName;

    public OfficerData(ServiceAddressData serviceAddress, LocalDateTime appointedOn, LocalDateTime resignedOn,
            String countryOfResidence, LinksData linksData, String nationality, String occupation, String officerRole,
            LocalDateTime dateOfBirth, IdentificationData identificationData, List<FormerNamesData> formerNameData,
            String surname, String forename, String otherForenames, String title, String companyName) {
        this.serviceAddress = serviceAddress;
        this.appointedOn = appointedOn;
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
        this.companyName = companyName;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServiceAddressData serviceAddress;
        private LocalDateTime appointedOn;
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
        private String companyName;

        public Builder withServiceAddress(ServiceAddressData serviceAddress) {
            this.serviceAddress = serviceAddress;
            return this;
        }

        public Builder withAppointedOn(LocalDateTime appointedOn) {
            this.appointedOn = appointedOn;
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

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public OfficerData build() {
            return new OfficerData(serviceAddress, appointedOn, resignedOn, countryOfResidence, linksData, nationality,
                    occupation, officerRole, dateOfBirth, identificationData, formerNameData, surname, forename,
                    otherForenames, title, companyName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfficerData)) return false;
        OfficerData that = (OfficerData) o;
        return Objects.equals(getServiceAddress(), that.getServiceAddress()) &&
                Objects.equals(getAppointedOn(), that.getAppointedOn()) &&
                Objects.equals(getResignedOn(), that.getResignedOn()) &&
                Objects.equals(getCountryOfResidence(), that.getCountryOfResidence()) &&
                Objects.equals(getLinksData(), that.getLinksData()) &&
                Objects.equals(getNationality(), that.getNationality()) &&
                Objects.equals(getOccupation(), that.getOccupation()) &&
                Objects.equals(getOfficerRole(), that.getOfficerRole()) &&
                Objects.equals(getDateOfBirth(), that.getDateOfBirth()) &&
                Objects.equals(getIdentificationData(), that.getIdentificationData()) &&
                Objects.equals(getFormerNameData(), that.getFormerNameData()) &&
                Objects.equals(getSurname(), that.getSurname()) &&
                Objects.equals(getForename(), that.getForename()) &&
                Objects.equals(getOtherForenames(), that.getOtherForenames()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getCompanyName(), that.getCompanyName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceAddress(), getAppointedOn(), getResignedOn(), getCountryOfResidence(),
                getLinksData(), getNationality(), getOccupation(), getOfficerRole(), getDateOfBirth(),
                getIdentificationData(), getFormerNameData(), getSurname(), getForename(), getOtherForenames(),
                getTitle(), getCompanyName());
    }
}
