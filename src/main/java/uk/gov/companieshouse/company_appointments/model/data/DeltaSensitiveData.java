package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.Valid;

public class DeltaSensitiveData {
  @Field("usual_residential_address")
  private DeltaUsualResidentialAddress usualResidentialAddress;

  @Field("residential_address_same_as_service_address")
  private Boolean residentialAddressSameAsServiceAddress;

  @Field("date_of_birth")
  private DeltaDateOfBirth dateOfBirth;

  public DeltaSensitiveData usualResidentialAddress(
          DeltaUsualResidentialAddress usualResidentialAddress) {
    this.usualResidentialAddress = usualResidentialAddress;
    return this;
  }

  @Valid
  public DeltaUsualResidentialAddress getUsualResidentialAddress() {
    return usualResidentialAddress;
  }

  public void setUsualResidentialAddress(DeltaUsualResidentialAddress usualResidentialAddress) {
    this.usualResidentialAddress = usualResidentialAddress;
  }

  public DeltaSensitiveData residentialAddressSameAsServiceAddress(Boolean residentialAddressSameAsServiceAddress) {
    this.residentialAddressSameAsServiceAddress = residentialAddressSameAsServiceAddress;
    return this;
  }

  public Boolean getResidentialAddressSameAsServiceAddress() {
    return residentialAddressSameAsServiceAddress;
  }

  public void setResidentialAddressSameAsServiceAddress(Boolean residentialAddressSameAsServiceAddress) {
    this.residentialAddressSameAsServiceAddress = residentialAddressSameAsServiceAddress;
  }

  public DeltaSensitiveData dateOfBirth(DeltaDateOfBirth dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  public DeltaDateOfBirth getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(DeltaDateOfBirth dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeltaSensitiveData sensitiveData = (DeltaSensitiveData) o;
    return Objects.equals(this.usualResidentialAddress, sensitiveData.usualResidentialAddress) &&
        Objects.equals(this.residentialAddressSameAsServiceAddress, sensitiveData.residentialAddressSameAsServiceAddress) &&
        Objects.equals(this.dateOfBirth, sensitiveData.dateOfBirth);
  }

  @Override
  public int hashCode() {
    return Objects.hash(usualResidentialAddress, residentialAddressSameAsServiceAddress, dateOfBirth);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SensitiveData {\n");
    
    sb.append("    usualResidentialAddress: ").append(toIndentedString(usualResidentialAddress)).append("\n");
    sb.append("    residentialAddressSameAsServiceAddress: ").append(toIndentedString(residentialAddressSameAsServiceAddress)).append("\n");
    sb.append("    dateOfBirth: ").append(toIndentedString(dateOfBirth)).append("\n");
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

