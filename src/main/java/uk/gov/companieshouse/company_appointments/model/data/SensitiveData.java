package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.Valid;

public class SensitiveData   {
  @Field("usual_residential_address")
  private UsualResidentialAddress usualResidentialAddress;

  @Field("residential_address_same_as_service_address")
  private Boolean residentialAddressSameAsServiceAddress;

  @Field("date_of_birth")
  private DateOfBirth dateOfBirth;

  public SensitiveData usualResidentialAddress(UsualResidentialAddress usualResidentialAddress) {
    this.usualResidentialAddress = usualResidentialAddress;
    return this;
  }

  @Valid
  public UsualResidentialAddress getUsualResidentialAddress() {
    return usualResidentialAddress;
  }

  public void setUsualResidentialAddress(UsualResidentialAddress usualResidentialAddress) {
    this.usualResidentialAddress = usualResidentialAddress;
  }

  public SensitiveData residentialAddressSameAsServiceAddress(Boolean residentialAddressSameAsServiceAddress) {
    this.residentialAddressSameAsServiceAddress = residentialAddressSameAsServiceAddress;
    return this;
  }

  public Boolean getResidentialAddressSameAsServiceAddress() {
    return residentialAddressSameAsServiceAddress;
  }

  public void setResidentialAddressSameAsServiceAddress(Boolean residentialAddressSameAsServiceAddress) {
    this.residentialAddressSameAsServiceAddress = residentialAddressSameAsServiceAddress;
  }

  public SensitiveData dateOfBirth(DateOfBirth dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  public DateOfBirth getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(DateOfBirth dateOfBirth) {
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
    SensitiveData sensitiveData = (SensitiveData) o;
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

