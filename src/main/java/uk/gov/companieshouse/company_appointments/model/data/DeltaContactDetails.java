package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class DeltaContactDetails {
  @Field("contact_name")
  private String contactName;

  public String getContactName() {
    return contactName;
  }

  public DeltaContactDetails setContactName(String contactName) {
    this.contactName = contactName;
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
    DeltaContactDetails contactDetails = (DeltaContactDetails) o;
    return Objects.equals(this.contactName, contactDetails.contactName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contactName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContactDetails {\n");
    
    sb.append("    contactName: ").append(toIndentedString(contactName)).append("\n");
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

