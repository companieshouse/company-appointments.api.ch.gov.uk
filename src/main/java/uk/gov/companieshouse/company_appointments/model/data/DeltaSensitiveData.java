package uk.gov.companieshouse.company_appointments.model.data;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class DeltaSensitiveData {
  @Field("usual_residential_address")
  private DeltaUsualResidentialAddress usualResidentialAddress;
  @Field("residential_address_same_as_service_address")
  private Boolean residentialAddressSameAsServiceAddress;
  @Field("date_of_birth")
  private Instant dateOfBirth;

  public DeltaUsualResidentialAddress getUsualResidentialAddress() {
    return usualResidentialAddress;
  }

  public DeltaSensitiveData setUsualResidentialAddress(DeltaUsualResidentialAddress usualResidentialAddress) {
    this.usualResidentialAddress = usualResidentialAddress;
    return this;
  }

  public Boolean getResidentialAddressSameAsServiceAddress() {
    return residentialAddressSameAsServiceAddress;
  }

  public DeltaSensitiveData setResidentialAddressSameAsServiceAddress(Boolean residentialAddressSameAsServiceAddress) {
    this.residentialAddressSameAsServiceAddress = residentialAddressSameAsServiceAddress;
    return this;
  }

  public Instant getDateOfBirth() {
    return dateOfBirth;
  }

  public DeltaSensitiveData setDateOfBirth(Instant dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
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
    return "class SensitiveData {\n"
            + "    usualResidentialAddress: " + toIndentedString(usualResidentialAddress) + "\n"
            + "    residentialAddressSameAsServiceAddress: " + toIndentedString(residentialAddressSameAsServiceAddress) + "\n"
            + "    dateOfBirth: " + toIndentedString(dateOfBirth) + "\n"
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

