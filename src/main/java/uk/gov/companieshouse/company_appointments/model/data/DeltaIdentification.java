package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Identification
 */
public class DeltaIdentification {
  @Field("identification_type")
  private String identificationType;
  @Field("legal_authority")
  private String legalAuthority;
  @Field("legal_form")
  private String legalForm;
  @Field("place_registered")
  private String placeRegistered;
  @Field("registration_number")
  private String registrationNumber;

  public String getIdentificationType() {
    return identificationType;
  }

  public DeltaIdentification setIdentificationType(String identificationType) {
    this.identificationType = identificationType;
    return this;
  }

  public String getLegalAuthority() {
    return legalAuthority;
  }

  public DeltaIdentification setLegalAuthority(String legalAuthority) {
    this.legalAuthority = legalAuthority;
    return this;
  }

  public String getLegalForm() {
    return legalForm;
  }

  public DeltaIdentification setLegalForm(String legalForm) {
    this.legalForm = legalForm;
    return this;
  }

  public String getPlaceRegistered() {
    return placeRegistered;
  }

  public DeltaIdentification setPlaceRegistered(String placeRegistered) {
    this.placeRegistered = placeRegistered;
    return this;
  }

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public DeltaIdentification setRegistrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
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
    DeltaIdentification identification = (DeltaIdentification) o;
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

