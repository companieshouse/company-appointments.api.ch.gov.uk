package uk.gov.companieshouse.company_appointments.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Identification
 */
public class DeltaIdentification {
  @JsonProperty("identification_type")
  @Field("identification_type")
  private String identificationType;
  @JsonProperty("legal_authority")
  @Field("legal_authority")
  private String legalAuthority;
  @JsonProperty("legal_form")
  @Field("legal_form")
  private String legalForm;
  @JsonProperty("place_registered")
  @Field("place_registered")
  private String placeRegistered;
  @JsonProperty("registration_number")
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
    return "class Identification {\n"
            + "    identificationType: " + toIndentedString(identificationType) + "\n"
            + "    legalAuthority: " + toIndentedString(legalAuthority) + "\n"
            + "    legalForm: " + toIndentedString(legalForm) + "\n"
            + "    placeRegistered: " + toIndentedString(placeRegistered) + "\n"
            + "    registrationNumber: " + toIndentedString(registrationNumber) + "\n"
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
}

