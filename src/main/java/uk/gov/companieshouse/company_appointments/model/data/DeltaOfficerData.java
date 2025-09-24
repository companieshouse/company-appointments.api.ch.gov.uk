package uk.gov.companieshouse.company_appointments.model.data;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.lang.Nullable;

public class DeltaOfficerData {
  @Field("person_number")
  private String personNumber;

  @Field("etag")
  private String etag;

  @Field("service_address")
  private DeltaServiceAddress serviceAddress;

  @Field("service_address_is_same_as_registered_office_address")
  private Boolean serviceAddressIsSameAsRegisteredOfficeAddress;

  @Field("country_of_residence")
  private String countryOfResidence;

  @Field("appointed_on")
  private Instant appointedOn;

  @Field("appointed_before")
  private Instant appointedBefore;

  @Field("is_pre_1992_appointment")
  private Boolean isPre1992Appointment;

  @Field("links")
  @Valid
  private DeltaItemLinkTypes links;

  @Field("nationality")
  private String nationality;

  @Field("occupation")
  private String occupation;

  @Field("officer_role")
  private String officerRole;

  @Field("is_secure_officer")
  private Boolean isSecureOfficer;

  @Field("identification")
  private DeltaIdentification identification;

  @Field("identity_verification_details")
  @Nullable
  private DeltaIdentityVerificationDetails identityVerificationDetails;

  @Field("company_name")
  private String companyName;

  @Field("surname")
  private String surname;

  @Field("forename")
  private String forename;

  @Field("honours")
  private String honours;

  @Field("other_forenames")
  private String otherForenames;

  @Field("title")
  private String title;

  @Field("company_number")
  private String companyNumber;

  @Field("contact_details")
  private DeltaContactDetails contactDetails;

  @Field("principal_office_address")
  private DeltaPrincipalOfficeAddress principalOfficeAddress;

  @Field("resigned_on")
  private Instant resignedOn;

  @Field("responsibilities")
  private String responsibilities;

  @Field("former_names")
  @Valid
  private List<DeltaFormerNames> formerNames = null;

  public DeltaOfficerData() {
  }

  private DeltaOfficerData(Builder builder) {
    personNumber = builder.personNumber;
    etag = builder.etag;
    serviceAddress = builder.serviceAddress;
    serviceAddressIsSameAsRegisteredOfficeAddress = builder.serviceAddressIsSameAsRegisteredOfficeAddress;
    countryOfResidence = builder.countryOfResidence;
    appointedOn = builder.appointedOn;
    appointedBefore = builder.appointedBefore;
    isPre1992Appointment = builder.isPre1992Appointment;
    links = builder.links;
    nationality = builder.nationality;
    occupation = builder.occupation;
    officerRole = builder.officerRole;
    isSecureOfficer = builder.isSecureOfficer;
    identification = builder.identification;
    identityVerificationDetails = builder.identityVerificationDetails;
    companyName = builder.companyName;
    surname = builder.surname;
    forename = builder.forename;
    honours = builder.honours;
    otherForenames = builder.otherForenames;
    title = builder.title;
    companyNumber = builder.companyNumber;
    contactDetails = builder.contactDetails;
    principalOfficeAddress = builder.principalOfficeAddress;
    resignedOn = builder.resignedOn;
    responsibilities = builder.responsibilities;
    formerNames = builder.formerNames;
  }

  public String getPersonNumber() {
    return personNumber;
  }

  public DeltaOfficerData setPersonNumber(String personNumber) {
    this.personNumber = personNumber;
    return this;
  }

  public String getEtag() {
    return etag;
  }

  public DeltaOfficerData setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  public DeltaServiceAddress getServiceAddress() {
    return serviceAddress;
  }

  public DeltaOfficerData setServiceAddress(
          DeltaServiceAddress serviceAddress) {
    this.serviceAddress = serviceAddress;
    return this;
  }

  public Boolean getServiceAddressIsSameAsRegisteredOfficeAddress() {
    return serviceAddressIsSameAsRegisteredOfficeAddress;
  }

  public DeltaOfficerData setServiceAddressIsSameAsRegisteredOfficeAddress(
          Boolean serviceAddressIsSameAsRegisteredOfficeAddress) {
    this.serviceAddressIsSameAsRegisteredOfficeAddress = serviceAddressIsSameAsRegisteredOfficeAddress;
    return this;
  }

  public String getCountryOfResidence() {
    return countryOfResidence;
  }

  public DeltaOfficerData setCountryOfResidence(String countryOfResidence) {
    this.countryOfResidence = countryOfResidence;
    return this;
  }

  public Instant getAppointedOn() {
    return appointedOn;
  }

  public DeltaOfficerData setAppointedOn(Instant appointedOn) {
    this.appointedOn = appointedOn;
    return this;
  }

  public Instant getAppointedBefore() {
    return appointedBefore;
  }

  public DeltaOfficerData setAppointedBefore(Instant appointedBefore) {
    this.appointedBefore = appointedBefore;
    return this;
  }

  public Boolean getPre1992Appointment() {
    return isPre1992Appointment;
  }

  public DeltaOfficerData setPre1992Appointment(Boolean pre1992Appointment) {
    isPre1992Appointment = pre1992Appointment;
    return this;
  }

  public DeltaItemLinkTypes getLinks() {
    return links;
  }

  public DeltaOfficerData setLinks(DeltaItemLinkTypes links) {
    this.links = links;
    return this;
  }

  public String getNationality() {
    return nationality;
  }

  public DeltaOfficerData setNationality(String nationality) {
    this.nationality = nationality;
    return this;
  }

  public String getOccupation() {
    return occupation;
  }

  public DeltaOfficerData setOccupation(String occupation) {
    this.occupation = occupation;
    return this;
  }

  public String getOfficerRole() {
    return officerRole;
  }

  public DeltaOfficerData setOfficerRole(String officerRole) {
    this.officerRole = officerRole;
    return this;
  }

  public Boolean getSecureOfficer() {
    return isSecureOfficer;
  }

  public DeltaOfficerData setSecureOfficer(Boolean secureOfficer) {
    isSecureOfficer = secureOfficer;
    return this;
  }

  public DeltaIdentification getIdentification() {
    return identification;
  }

  public DeltaOfficerData setIdentification(
          DeltaIdentification identification) {
    this.identification = identification;
    return this;
  }

  public DeltaIdentityVerificationDetails getIdentityVerificationDetails() { return identityVerificationDetails; }

  public DeltaOfficerData setIdentityVerificationDetails(
          DeltaIdentityVerificationDetails identityVerificationDetails) {
    this.identityVerificationDetails = identityVerificationDetails;
    return this;
  }

  public String getCompanyName() {
    return companyName;
  }

  public DeltaOfficerData setCompanyName(String companyName) {
    this.companyName = companyName;
    return this;
  }

  public String getSurname() {
    return surname;
  }

  public DeltaOfficerData setSurname(String surname) {
    this.surname = surname;
    return this;
  }

  public String getForename() {
    return forename;
  }

  public DeltaOfficerData setForename(String forename) {
    this.forename = forename;
    return this;
  }

  public String getHonours() {
    return honours;
  }

  public DeltaOfficerData setHonours(String honours) {
    this.honours = honours;
    return this;
  }

  public String getOtherForenames() {
    return otherForenames;
  }

  public DeltaOfficerData setOtherForenames(String otherForenames) {
    this.otherForenames = otherForenames;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public DeltaOfficerData setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getCompanyNumber() {
    return companyNumber;
  }

  public DeltaOfficerData setCompanyNumber(String companyNumber) {
    this.companyNumber = companyNumber;
    return this;
  }

  public DeltaContactDetails getContactDetails() {
    return contactDetails;
  }

  public DeltaOfficerData setContactDetails(
          DeltaContactDetails contactDetails) {
    this.contactDetails = contactDetails;
    return this;
  }

  public DeltaPrincipalOfficeAddress getPrincipalOfficeAddress() {
    return principalOfficeAddress;
  }

  public DeltaOfficerData setPrincipalOfficeAddress(
          DeltaPrincipalOfficeAddress principalOfficeAddress) {
    this.principalOfficeAddress = principalOfficeAddress;
    return this;
  }

  public Instant getResignedOn() {
    return resignedOn;
  }

  public DeltaOfficerData setResignedOn(Instant resignedOn) {
    this.resignedOn = resignedOn;
    return this;
  }

  public String getResponsibilities() {
    return responsibilities;
  }

  public DeltaOfficerData setResponsibilities(String responsibilities) {
    this.responsibilities = responsibilities;
    return this;
  }

  public List<DeltaFormerNames> getFormerNames() {
    return formerNames;
  }

  public DeltaOfficerData setFormerNames(
          List<DeltaFormerNames> formerNames) {
    this.formerNames = formerNames;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeltaOfficerData data = (DeltaOfficerData) o;
    return Objects.equals(this.personNumber, data.personNumber) &&
        Objects.equals(this.etag, data.etag) &&
        Objects.equals(this.serviceAddress, data.serviceAddress) &&
        Objects.equals(this.serviceAddressIsSameAsRegisteredOfficeAddress, data.serviceAddressIsSameAsRegisteredOfficeAddress) &&
        Objects.equals(this.countryOfResidence, data.countryOfResidence) &&
        Objects.equals(this.appointedOn, data.appointedOn) &&
        Objects.equals(this.appointedBefore, data.appointedBefore) &&
        Objects.equals(this.isPre1992Appointment, data.isPre1992Appointment) &&
        Objects.equals(this.links, data.links) &&
        Objects.equals(this.nationality, data.nationality) &&
        Objects.equals(this.occupation, data.occupation) &&
        Objects.equals(this.officerRole, data.officerRole) &&
        Objects.equals(this.isSecureOfficer, data.isSecureOfficer) &&
        Objects.equals(this.identification, data.identification) &&
        Objects.equals(this.identityVerificationDetails, data.identityVerificationDetails) &&
        Objects.equals(this.companyName, data.companyName) &&
        Objects.equals(this.surname, data.surname) &&
        Objects.equals(this.forename, data.forename) &&
        Objects.equals(this.honours, data.honours) &&
        Objects.equals(this.otherForenames, data.otherForenames) &&
        Objects.equals(this.title, data.title) &&
        Objects.equals(this.companyNumber, data.companyNumber) &&
        Objects.equals(this.contactDetails, data.contactDetails) &&
        Objects.equals(this.principalOfficeAddress, data.principalOfficeAddress) &&
        Objects.equals(this.resignedOn, data.resignedOn) &&
        Objects.equals(this.responsibilities, data.responsibilities) &&
        Objects.equals(this.formerNames, data.formerNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(personNumber, etag, serviceAddress, serviceAddressIsSameAsRegisteredOfficeAddress, countryOfResidence, appointedOn, appointedBefore, isPre1992Appointment, links, nationality, occupation, officerRole, isSecureOfficer, identification, identityVerificationDetails, companyName, surname, forename, honours, otherForenames, title, companyNumber, contactDetails, principalOfficeAddress, resignedOn, responsibilities, formerNames);
  }

  @Override
  public String toString() {
    return "class Data {\n"
            + "    personNumber: " + toIndentedString(personNumber) + "\n"
            + "    etag: " + toIndentedString(etag) + "\n"
            + "    serviceAddress: " + toIndentedString(serviceAddress) + "\n"
            + "    serviceAddressIsSameAsRegisteredOfficeAddress: " + toIndentedString(
            serviceAddressIsSameAsRegisteredOfficeAddress) + "\n"
            + "    countryOfResidence: " + toIndentedString(countryOfResidence) + "\n"
            + "    appointedOn: " + toIndentedString(appointedOn) + "\n"
            + "    appointedBefore: " + toIndentedString(appointedBefore) + "\n"
            + "    isPre1992Appointment: " + toIndentedString(isPre1992Appointment) + "\n"
            + "    links: " + toIndentedString(links) + "\n"
            + "    nationality: " + toIndentedString(nationality) + "\n"
            + "    occupation: " + toIndentedString(occupation) + "\n"
            + "    officerRole: " + toIndentedString(officerRole) + "\n"
            + "    isSecureOfficer: " + toIndentedString(isSecureOfficer) + "\n"
            + "    identification: " + toIndentedString(identification) + "\n"
            + "    identityVerificationDetails: " + toIndentedString(identityVerificationDetails) + "\n"
            + "    companyName: " + toIndentedString(companyName) + "\n"
            + "    surname: " + toIndentedString(surname) + "\n"
            + "    forename: " + toIndentedString(forename) + "\n"
            + "    honours: " + toIndentedString(honours) + "\n"
            + "    otherForenames: " + toIndentedString(otherForenames) + "\n"
            + "    title: " + toIndentedString(title) + "\n"
            + "    companyNumber: " + toIndentedString(companyNumber) + "\n"
            + "    contactDetails: " + toIndentedString(contactDetails) + "\n"
            + "    principalOfficeAddress: " + toIndentedString(principalOfficeAddress) + "\n"
            + "    resignedOn: " + toIndentedString(resignedOn) + "\n"
            + "    responsibilities: " + toIndentedString(responsibilities) + "\n"
            + "    formerNames: " + toIndentedString(formerNames) + "\n"
            + "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public static final class Builder {

    private String personNumber;
    private String etag;
    private DeltaServiceAddress serviceAddress;
    private Boolean serviceAddressIsSameAsRegisteredOfficeAddress;
    private String countryOfResidence;
    private Instant appointedOn;
    private Instant appointedBefore;
    private Boolean isPre1992Appointment;
    private @Valid DeltaItemLinkTypes links;
    private String nationality;
    private String occupation;
    private String officerRole;
    private Boolean isSecureOfficer;
    private DeltaIdentification identification;
    private DeltaIdentityVerificationDetails identityVerificationDetails;
    private String companyName;
    private String surname;
    private String forename;
    private String honours;
    private String otherForenames;
    private String title;
    private String companyNumber;
    private DeltaContactDetails contactDetails;
    private DeltaPrincipalOfficeAddress principalOfficeAddress;
    private Instant resignedOn;
    private String responsibilities;
    private @Valid List<DeltaFormerNames> formerNames;

    private Builder() {
    }

    public static Builder builder() {
      return new Builder();
    }

    public Builder personNumber(String personNumber) {
      this.personNumber = personNumber;
      return this;
    }

    public Builder etag(String etag) {
      this.etag = etag;
      return this;
    }

    public Builder serviceAddress(DeltaServiceAddress serviceAddress) {
      this.serviceAddress = serviceAddress;
      return this;
    }

    public Builder serviceAddressIsSameAsRegisteredOfficeAddress(Boolean serviceAddressIsSameAsRegisteredOfficeAddress) {
      this.serviceAddressIsSameAsRegisteredOfficeAddress = serviceAddressIsSameAsRegisteredOfficeAddress;
      return this;
    }

    public Builder countryOfResidence(String countryOfResidence) {
      this.countryOfResidence = countryOfResidence;
      return this;
    }

    public Builder appointedOn(Instant appointedOn) {
      this.appointedOn = appointedOn;
      return this;
    }

    public Builder appointedBefore(Instant appointedBefore) {
      this.appointedBefore = appointedBefore;
      return this;
    }

    public Builder isPre1992Appointment(Boolean isPre1992Appointment) {
      this.isPre1992Appointment = isPre1992Appointment;
      return this;
    }

    public Builder links(@Valid DeltaItemLinkTypes links) {
      this.links = links;
      return this;
    }

    public Builder nationality(String nationality) {
      this.nationality = nationality;
      return this;
    }

    public Builder occupation(String occupation) {
      this.occupation = occupation;
      return this;
    }

    public Builder officerRole(String officerRole) {
      this.officerRole = officerRole;
      return this;
    }

    public Builder isSecureOfficer(Boolean isSecureOfficer) {
      this.isSecureOfficer = isSecureOfficer;
      return this;
    }

    public Builder identification(DeltaIdentification identification) {
      this.identification = identification;
      return this;
    }

    public Builder identityVerificationDetails(DeltaIdentityVerificationDetails identityVerificationDetails) {
      this.identityVerificationDetails = identityVerificationDetails;
      return this;
    }

    public Builder companyName(String companyName) {
      this.companyName = companyName;
      return this;
    }

    public Builder surname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder forename(String forename) {
      this.forename = forename;
      return this;
    }

    public Builder honours(String honours) {
      this.honours = honours;
      return this;
    }

    public Builder otherForenames(String otherForenames) {
      this.otherForenames = otherForenames;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder companyNumber(String companyNumber) {
      this.companyNumber = companyNumber;
      return this;
    }

    public Builder contactDetails(DeltaContactDetails contactDetails) {
      this.contactDetails = contactDetails;
      return this;
    }

    public Builder principalOfficeAddress(DeltaPrincipalOfficeAddress principalOfficeAddress) {
      this.principalOfficeAddress = principalOfficeAddress;
      return this;
    }

    public Builder resignedOn(Instant resignedOn) {
      this.resignedOn = resignedOn;
      return this;
    }

    public Builder responsibilities(String responsibilities) {
      this.responsibilities = responsibilities;
      return this;
    }

    public Builder formerNames(@Valid List<DeltaFormerNames> formerNames) {
      this.formerNames = formerNames;
      return this;
    }

    public DeltaOfficerData build() {
      return new DeltaOfficerData(this);
    }
  }
}
