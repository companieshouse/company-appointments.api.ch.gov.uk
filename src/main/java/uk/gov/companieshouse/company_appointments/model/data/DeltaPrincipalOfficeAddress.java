package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class DeltaPrincipalOfficeAddress {
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

  public DeltaPrincipalOfficeAddress setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
    return this;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public DeltaPrincipalOfficeAddress setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
    return this;
  }

  public String getCareOf() {
    return careOf;
  }

  public DeltaPrincipalOfficeAddress setCareOf(String careOf) {
    this.careOf = careOf;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public DeltaPrincipalOfficeAddress setCountry(String country) {
    this.country = country;
    return this;
  }

  public String getLocality() {
    return locality;
  }

  public DeltaPrincipalOfficeAddress setLocality(String locality) {
    this.locality = locality;
    return this;
  }

  public String getPoBox() {
    return poBox;
  }

  public DeltaPrincipalOfficeAddress setPoBox(String poBox) {
    this.poBox = poBox;
    return this;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public DeltaPrincipalOfficeAddress setPostalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public String getPremises() {
    return premises;
  }

  public DeltaPrincipalOfficeAddress setPremises(String premises) {
    this.premises = premises;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public DeltaPrincipalOfficeAddress setRegion(String region) {
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
    DeltaPrincipalOfficeAddress principalOfficeAddress = (DeltaPrincipalOfficeAddress) o;
    return Objects.equals(this.addressLine1, principalOfficeAddress.addressLine1) &&
        Objects.equals(this.addressLine2, principalOfficeAddress.addressLine2) &&
        Objects.equals(this.careOf, principalOfficeAddress.careOf) &&
        Objects.equals(this.country, principalOfficeAddress.country) &&
        Objects.equals(this.locality, principalOfficeAddress.locality) &&
        Objects.equals(this.poBox, principalOfficeAddress.poBox) &&
        Objects.equals(this.postalCode, principalOfficeAddress.postalCode) &&
        Objects.equals(this.premises, principalOfficeAddress.premises) &&
        Objects.equals(this.region, principalOfficeAddress.region);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressLine1, addressLine2, careOf, country, locality, poBox, postalCode, premises, region);
  }

  @Override
  public String toString() {
    return "class PrincipalOfficeAddress {\n"
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

