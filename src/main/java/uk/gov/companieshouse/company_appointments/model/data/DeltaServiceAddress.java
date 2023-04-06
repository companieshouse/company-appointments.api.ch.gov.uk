package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * ServiceAddress
 */
public class DeltaServiceAddress {
  @Field("address_line_1")
  private String addressLine1;
  @Field("address_line_2")
  private String addressLine2;
  @Field("country")
  private String country;
  @Field("locality")
  private String locality;
  @Field("postal_code")
  private String postalCode;
  @Field("premises")
  private String premises;
  @Field("region")
  private String region;

  public String getAddressLine1() {
    return addressLine1;
  }

  public DeltaServiceAddress setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
    return this;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public DeltaServiceAddress setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public DeltaServiceAddress setCountry(String country) {
    this.country = country;
    return this;
  }

  public String getLocality() {
    return locality;
  }

  public DeltaServiceAddress setLocality(String locality) {
    this.locality = locality;
    return this;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public DeltaServiceAddress setPostalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public String getPremises() {
    return premises;
  }

  public DeltaServiceAddress setPremises(String premises) {
    this.premises = premises;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public DeltaServiceAddress setRegion(String region) {
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
    DeltaServiceAddress serviceAddress = (DeltaServiceAddress) o;
    return Objects.equals(this.addressLine1, serviceAddress.addressLine1) &&
        Objects.equals(this.addressLine2, serviceAddress.addressLine2) &&
        Objects.equals(this.country, serviceAddress.country) &&
        Objects.equals(this.locality, serviceAddress.locality) &&
        Objects.equals(this.postalCode, serviceAddress.postalCode) &&
        Objects.equals(this.premises, serviceAddress.premises) &&
        Objects.equals(this.region, serviceAddress.region);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressLine1, addressLine2, country, locality, postalCode, premises, region);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceAddress {\n");
    
    sb.append("    addressLine1: ").append(toIndentedString(addressLine1)).append("\n");
    sb.append("    addressLine2: ").append(toIndentedString(addressLine2)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    locality: ").append(toIndentedString(locality)).append("\n");
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
