package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import javax.validation.Valid;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * ItemLinkTypes
 */
public class ItemLinkTypes   {
  @Field("self")
  private String self;

  @Field("officer")
  private OfficerLinkTypes officer;

  public ItemLinkTypes self(String self) {
    this.self = self;
    return this;
  }

  public String getSelf() {
    return self;
  }

  public void setSelf(String self) {
    this.self = self;
  }

  public ItemLinkTypes officer(OfficerLinkTypes officer) {
    this.officer = officer;
    return this;
  }

  @Valid
  public OfficerLinkTypes getOfficer() {
    return officer;
  }

  public void setOfficer(OfficerLinkTypes officer) {
    this.officer = officer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemLinkTypes itemLinkTypes = (ItemLinkTypes) o;
    return Objects.equals(this.self, itemLinkTypes.self) &&
        Objects.equals(this.officer, itemLinkTypes.officer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, officer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ItemLinkTypes {\n");
    
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    officer: ").append(toIndentedString(officer)).append("\n");
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
