package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.Valid;

public class DeltaOfficerData {
  @Field("person_number")
  private String personNumber;

  @Field("service_address")
  private DeltaServiceAddress serviceAddress;

  @Field("service_address_same_as_registered_office_address")
  private Boolean serviceAddressSameAsRegisteredOfficeAddress;

  @Field("country_of_residence")
  private String countryOfResidence;

  @Field("appointed_on")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private LocalDate appointedOn;

  @Field("appointed_before")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private LocalDate appointedBefore;

  @Field("is_pre_1992_appointment")
  private Boolean isPre1992Appointment;

  @Field("links")
  @Valid
  private DeltaItemLinkTypes links;

  @Field("nationality")
  private String nationality;

  @Field("occupation")
  private String occupation;

  /**
   * Gets or Sets officerRole
   */
  public enum OfficerRoleEnum {
    CIC_MANAGER("cic-manager"),
    
    CORPORATE_DIRECTOR("corporate-director"),
    
    CORPORATE_LLP_DESIGNATED_MEMBER("corporate-llp-designated-member"),
    
    CORPORATE_LLP_MEMBER("corporate-llp-member"),
    
    CORPORATE_MANAGER_OF_AN_EEIG("corporate-manager-of-an-eeig"),
    
    CORPORATE_MANAGING_OFFICER("corporate-managing-officer"),
    
    CORPORATE_MEMBER_OF_A_MANAGEMENT_ORGAN("corporate-member-of-a-management-organ"),
    
    CORPORATE_MEMBER_OF_A_SUPERVISORY_ORGAN("corporate-member-of-a-supervisory-organ"),
    
    CORPORATE_MEMBER_OF_AN_ADMINISTRATIVE_ORGAN("corporate-member-of-an-administrative-organ"),
    
    CORPORATE_NOMINEE_DIRECTOR("corporate-nominee-director"),
    
    CORPORATE_NOMINEE_SECRETARY("corporate-nominee-secretary"),
    
    CORPORATE_SECRETARY("corporate-secretary"),
    
    DIRECTOR("director"),
    
    GENERAL_PARTNER_IN_A_LIMITED_PARTNERSHIP("general-partner-in-a-limited-partnership"),
    
    JUDICIAL_FACTOR("judicial-factor"),
    
    LIMITED_PARTNER_IN_A_LIMITED_PARTNERSHIP("limited-partner-in-a-limited-partnership"),
    
    LLP_DESIGNATED_MEMBER("llp-designated-member"),
    
    LLP_MEMBER("llp-member"),
    
    MANAGER_OF_AN_EEIG("manager-of-an-eeig"),
    
    MANAGING_OFFICER("managing-officer"),
    
    MEMBER_OF_A_MANAGEMENT_ORGAN("member-of-a-management-organ"),
    
    MEMBER_OF_A_SUPERVISORY_ORGAN("member-of-a-supervisory-organ"),
    
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN("member-of-an-administrative-organ"),
    
    NOMINEE_DIRECTOR("nominee-director"),
    
    NOMINEE_SECRETARY("nominee-secretary"),
    
    PERSON_AUTHORISED_TO_ACCEPT("person-authorised-to-accept"),
    
    PERSON_AUTHORISED_TO_REPRESENT("person-authorised-to-represent"),
    
    PERSON_AUTHORISED_TO_REPRESENT_AND_ACCEPT("person-authorised-to-represent-and-accept"),
    
    RECEIVER_AND_MANAGER("receiver-and-manager"),
    
    SECRETARY("secretary");

    private String value;

    OfficerRoleEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static OfficerRoleEnum fromValue(String value) {
      for (OfficerRoleEnum b : OfficerRoleEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Field("officer_role")
  private OfficerRoleEnum officerRole;

  @Field("is_secure_officer")
  private Boolean isSecureOfficer;

  @Field("identification")
  private DeltaIdentification identification;

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
  private ContactDetails contactDetails;

  @Field("principal_office_address")
  private DeltaPrincipalOfficeAddress principalOfficeAddress;

  @Field("resigned_on")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private LocalDate resignedOn;

  @Field("responsibilities")
  private String responsibilities;

  @Field("former_names")
  @Valid
  private List<DeltaFormerNames> formerNames = null;

  public String getPersonNumber() {
    return personNumber;
  }

  public DeltaOfficerData setPersonNumber(String personNumber) {
    this.personNumber = personNumber;
    return this;
  }

  public DeltaServiceAddress getServiceAddress() {
    return serviceAddress;
  }

  public DeltaOfficerData setServiceAddress(DeltaServiceAddress serviceAddress) {
    this.serviceAddress = serviceAddress;
    return this;
  }

  public Boolean getServiceAddressSameAsRegisteredOfficeAddress() {
    return serviceAddressSameAsRegisteredOfficeAddress;
  }

  public DeltaOfficerData setServiceAddressSameAsRegisteredOfficeAddress(
          Boolean serviceAddressSameAsRegisteredOfficeAddress) {
    this.serviceAddressSameAsRegisteredOfficeAddress = serviceAddressSameAsRegisteredOfficeAddress;
    return this;
  }

  public String getCountryOfResidence() {
    return countryOfResidence;
  }

  public DeltaOfficerData setCountryOfResidence(String countryOfResidence) {
    this.countryOfResidence = countryOfResidence;
    return this;
  }

  public LocalDate getAppointedOn() {
    return appointedOn;
  }

  public DeltaOfficerData setAppointedOn(LocalDate appointedOn) {
    this.appointedOn = appointedOn;
    return this;
  }

  public LocalDate getAppointedBefore() {
    return appointedBefore;
  }

  public DeltaOfficerData setAppointedBefore(LocalDate appointedBefore) {
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

  public OfficerRoleEnum getOfficerRole() {
    return officerRole;
  }

  public DeltaOfficerData setOfficerRole(
          OfficerRoleEnum officerRole) {
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

  public DeltaOfficerData setIdentification(DeltaIdentification identification) {
    this.identification = identification;
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

  public ContactDetails getContactDetails() {
    return contactDetails;
  }

  public DeltaOfficerData setContactDetails(ContactDetails contactDetails) {
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

  public LocalDate getResignedOn() {
    return resignedOn;
  }

  public DeltaOfficerData setResignedOn(LocalDate resignedOn) {
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
        Objects.equals(this.serviceAddress, data.serviceAddress) &&
        Objects.equals(this.serviceAddressSameAsRegisteredOfficeAddress, data.serviceAddressSameAsRegisteredOfficeAddress) &&
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
    return Objects.hash(personNumber, serviceAddress, serviceAddressSameAsRegisteredOfficeAddress, countryOfResidence, appointedOn, appointedBefore, isPre1992Appointment, links, nationality, occupation, officerRole, isSecureOfficer, identification, companyName, surname, forename, honours, otherForenames, title, companyNumber, contactDetails, principalOfficeAddress, resignedOn, responsibilities, formerNames);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Data {\n");

    sb.append("    personNumber: ").append(toIndentedString(personNumber)).append("\n");
    sb.append("    serviceAddress: ").append(toIndentedString(serviceAddress)).append("\n");
    sb.append("    serviceAddressSameAsRegisteredOfficeAddress: ").append(toIndentedString(serviceAddressSameAsRegisteredOfficeAddress)).append("\n");
    sb.append("    countryOfResidence: ").append(toIndentedString(countryOfResidence)).append("\n");
    sb.append("    appointedOn: ").append(toIndentedString(appointedOn)).append("\n");
    sb.append("    appointedBefore: ").append(toIndentedString(appointedBefore)).append("\n");
    sb.append("    isPre1992Appointment: ").append(toIndentedString(isPre1992Appointment)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    nationality: ").append(toIndentedString(nationality)).append("\n");
    sb.append("    occupation: ").append(toIndentedString(occupation)).append("\n");
    sb.append("    officerRole: ").append(toIndentedString(officerRole)).append("\n");
    sb.append("    isSecureOfficer: ").append(toIndentedString(isSecureOfficer)).append("\n");
    sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
    sb.append("    companyName: ").append(toIndentedString(companyName)).append("\n");
    sb.append("    surname: ").append(toIndentedString(surname)).append("\n");
    sb.append("    forename: ").append(toIndentedString(forename)).append("\n");
    sb.append("    honours: ").append(toIndentedString(honours)).append("\n");
    sb.append("    otherForenames: ").append(toIndentedString(otherForenames)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    companyNumber: ").append(toIndentedString(companyNumber)).append("\n");
    sb.append("    contactDetails: ").append(toIndentedString(contactDetails)).append("\n");
    sb.append("    principalOfficeAddress: ").append(toIndentedString(principalOfficeAddress)).append("\n");
    sb.append("    resignedOn: ").append(toIndentedString(resignedOn)).append("\n");
    sb.append("    responsibilities: ").append(toIndentedString(responsibilities)).append("\n");
    sb.append("    formerNames: ").append(toIndentedString(formerNames)).append("\n");
    sb.append("}");
    return sb.toString();
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
}
