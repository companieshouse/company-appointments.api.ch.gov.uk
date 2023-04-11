package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * UsualResidentialAddress
 */
public class DeltaUsualResidentialAddress {
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

  public String getAddressLine1() {
    return addressLine1;
  }

  public DeltaUsualResidentialAddress setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
    return this;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public DeltaUsualResidentialAddress setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
    return this;
  }

  public String getCareOf() {
    return careOf;
  }

  public DeltaUsualResidentialAddress setCareOf(String careOf) {
    this.careOf = careOf;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public DeltaUsualResidentialAddress setCountry(String country) {
    this.country = country;
    return this;
  }

  public String getLocality() {
    return locality;
  }

  public DeltaUsualResidentialAddress setLocality(String locality) {
    this.locality = locality;
    return this;
  }

  public String getPoBox() {
    return poBox;
  }

  public DeltaUsualResidentialAddress setPoBox(String poBox) {
    this.poBox = poBox;
    return this;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public DeltaUsualResidentialAddress setPostalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public String getPremises() {
    return premises;
  }

  public DeltaUsualResidentialAddress setPremises(String premises) {
    this.premises = premises;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public DeltaUsualResidentialAddress setRegion(String region) {
    this.region = region;
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
    DeltaUsualResidentialAddress usualResidentialAddress = (DeltaUsualResidentialAddress) o;
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
    return "class UsualResidentialAddress {\n"
            + "    addressLine1: " + toIndentedString(addressLine1) + "\n"
            + "    addressLine2: " + toIndentedString(addressLine2) + "\n"
            + "    careOf: " + toIndentedString(careOf) + "\n"
            + "    country: " + toIndentedString(country) + "\n"
            + "    locality: " + toIndentedString(locality) + "\n"
            + "    poBox: " + toIndentedString(poBox) + "\n"
            + "    postalCode: " + toIndentedString(postalCode) + "\n"
            + "    premises: " + toIndentedString(premises) + "\n"
            + "    region: " + toIndentedString(region) + "\n"
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
