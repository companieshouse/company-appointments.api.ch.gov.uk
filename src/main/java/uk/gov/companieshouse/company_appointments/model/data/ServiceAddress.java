package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import javax.validation.constraints.*;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * ServiceAddress
 */
public class ServiceAddress   {
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

  public ServiceAddress addressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
    return this;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public ServiceAddress addressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
    return this;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  public ServiceAddress country(String country) {
    this.country = country;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public ServiceAddress locality(String locality) {
    this.locality = locality;
    return this;
  }

  @NotNull
  public String getLocality() {
    return locality;
  }

  public void setLocality(String locality) {
    this.locality = locality;
  }

  public ServiceAddress postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public ServiceAddress premises(String premises) {
    this.premises = premises;
    return this;
  }

  public String getPremises() {
    return premises;
  }

  public void setPremises(String premises) {
    this.premises = premises;
  }

  public ServiceAddress region(String region) {
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
    ServiceAddress serviceAddress = (ServiceAddress) o;
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
