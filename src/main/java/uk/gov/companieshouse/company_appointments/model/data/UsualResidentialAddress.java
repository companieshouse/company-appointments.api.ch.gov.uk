package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * UsualResidentialAddress
 */
public class UsualResidentialAddress   {
  @Field("address_line_1")
  private String addressLine1;

  @Field("address_line_2")
  private String addressLine2;

  @Field("care_of")
  private String careOf;

  @Field("country")
  private String country;

  @Field("locality")
  private String locality;

  @Field("po_box")
  private String poBox;

  @Field("postal_code")
  private String postalCode;

  @Field("premises")
  private String premises;

  @Field("region")
  private String region;

  public UsualResidentialAddress addressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
    return this;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public UsualResidentialAddress addressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
    return this;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  public UsualResidentialAddress careOf(String careOf) {
    this.careOf = careOf;
    return this;
  }

  public String getCareOf() {
    return careOf;
  }

  public void setCareOf(String careOf) {
    this.careOf = careOf;
  }

  public UsualResidentialAddress country(String country) {
    this.country = country;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public UsualResidentialAddress locality(String locality) {
    this.locality = locality;
    return this;
  }

  public String getLocality() {
    return locality;
  }

  public void setLocality(String locality) {
    this.locality = locality;
  }

  public UsualResidentialAddress poBox(String poBox) {
    this.poBox = poBox;
    return this;
  }

  public String getPoBox() {
    return poBox;
  }

  public void setPoBox(String poBox) {
    this.poBox = poBox;
  }

  public UsualResidentialAddress postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public UsualResidentialAddress premises(String premises) {
    this.premises = premises;
    return this;
  }

  public String getPremises() {
    return premises;
  }

  public void setPremises(String premises) {
    this.premises = premises;
  }

  public UsualResidentialAddress region(String region) {
    this.region = region;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UsualResidentialAddress usualResidentialAddress = (UsualResidentialAddress) o;
    return Objects.equals(this.addressLine1, usualResidentialAddress.addressLine1) &&
        Objects.equals(this.addressLine2, usualResidentialAddress.addressLine2) &&
        Objects.equals(this.careOf, usualResidentialAddress.careOf) &&
        Objects.equals(this.country, usualResidentialAddress.country) &&
        Objects.equals(this.locality, usualResidentialAddress.locality) &&
        Objects.equals(this.poBox, usualResidentialAddress.poBox) &&
        Objects.equals(this.postalCode, usualResidentialAddress.postalCode) &&
        Objects.equals(this.premises, usualResidentialAddress.premises) &&
        Objects.equals(this.region, usualResidentialAddress.region);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressLine1, addressLine2, careOf, country, locality, poBox, postalCode, premises, region);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UsualResidentialAddress {\n");
    
    sb.append("    addressLine1: ").append(toIndentedString(addressLine1)).append("\n");
    sb.append("    addressLine2: ").append(toIndentedString(addressLine2)).append("\n");
    sb.append("    careOf: ").append(toIndentedString(careOf)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    locality: ").append(toIndentedString(locality)).append("\n");
    sb.append("    poBox: ").append(toIndentedString(poBox)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    premises: ").append(toIndentedString(premises)).append("\n");
    sb.append("    region: ").append(toIndentedString(region)).append("\n");
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
