package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Identification
 */
public class Identification   {
  /**
   * The officer's identity type
   */
  public enum IdentificationTypeEnum {
    EEA("eea"),
    
    NON_EEA("non-eea"),
    
    UK_LIMITED("uk-limited"),
    
    OTHER_CORPORATE_BODY_OR_FIRM("other-corporate-body-or-firm"),
    
    REGISTERED_OVERSEAS_ENTITY_CORPORATE_MANAGING_OFFICER("registered-overseas-entity-corporate-managing-officer");

    private String value;

    IdentificationTypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static IdentificationTypeEnum fromValue(String value) {
      for (IdentificationTypeEnum b : IdentificationTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Field("identification_type")
  private IdentificationTypeEnum identificationType;

  @Field("legal_authority")
  private String legalAuthority;

  @Field("legal_form")
  private String legalForm;

  @Field("place_registered")
  private String placeRegistered;

  @Field("registration_number")
  private String registrationNumber;

  public Identification identificationType(IdentificationTypeEnum identificationType) {
    this.identificationType = identificationType;
    return this;
  }

  public IdentificationTypeEnum getIdentificationType() {
    return identificationType;
  }

  public void setIdentificationType(IdentificationTypeEnum identificationType) {
    this.identificationType = identificationType;
  }

  public Identification legalAuthority(String legalAuthority) {
    this.legalAuthority = legalAuthority;
    return this;
  }

  public String getLegalAuthority() {
    return legalAuthority;
  }

  public void setLegalAuthority(String legalAuthority) {
    this.legalAuthority = legalAuthority;
  }

  public Identification legalForm(String legalForm) {
    this.legalForm = legalForm;
    return this;
  }

  public String getLegalForm() {
    return legalForm;
  }

  public void setLegalForm(String legalForm) {
    this.legalForm = legalForm;
  }

  public Identification placeRegistered(String placeRegistered) {
    this.placeRegistered = placeRegistered;
    return this;
  }

  public String getPlaceRegistered() {
    return placeRegistered;
  }

  public void setPlaceRegistered(String placeRegistered) {
    this.placeRegistered = placeRegistered;
  }

  public Identification registrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
    return this;
  }

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Identification identification = (Identification) o;
    return Objects.equals(this.identificationType, identification.identificationType) &&
        Objects.equals(this.legalAuthority, identification.legalAuthority) &&
        Objects.equals(this.legalForm, identification.legalForm) &&
        Objects.equals(this.placeRegistered, identification.placeRegistered) &&
        Objects.equals(this.registrationNumber, identification.registrationNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identificationType, legalAuthority, legalForm, placeRegistered, registrationNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Identification {\n");
    
    sb.append("    identificationType: ").append(toIndentedString(identificationType)).append("\n");
    sb.append("    legalAuthority: ").append(toIndentedString(legalAuthority)).append("\n");
    sb.append("    legalForm: ").append(toIndentedString(legalForm)).append("\n");
    sb.append("    placeRegistered: ").append(toIndentedString(placeRegistered)).append("\n");
    sb.append("    registrationNumber: ").append(toIndentedString(registrationNumber)).append("\n");
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

