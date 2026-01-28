package uk.gov.companieshouse.company_appointments.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

/**
 * Contribution sub-type
 */
public class DeltaContributionSubType {
  @JsonProperty("sub_type")
  @Field("sub_type")
  private String subType;

  public DeltaContributionSubType() {
  }

  public DeltaContributionSubType(String subType) {
    this.subType = subType;
  }

  public String getSubType() {
    return subType;
  }

  public DeltaContributionSubType setSubType(String subType) {
    this.subType = subType;
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
    DeltaContributionSubType deltaSubType = (DeltaContributionSubType) o;
    return Objects.equals(this.subType, deltaSubType.subType) ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(subType);
  }

  @Override
  public String toString() {
    return "class SubType {\n"
            + "    sub type: " + toIndentedString(subType) + "\n"
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
