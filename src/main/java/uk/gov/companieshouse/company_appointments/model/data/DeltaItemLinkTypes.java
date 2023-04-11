package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * ItemLinkTypes
 */
public class DeltaItemLinkTypes {
  @Field("self")
  private String self;
  @Field("officer")
  private DeltaOfficerLinkTypes officer;

  public String getSelf() {
    return self;
  }

  public DeltaItemLinkTypes setSelf(String self) {
    this.self = self;
    return this;
  }

  public DeltaOfficerLinkTypes getOfficer() {
    return officer;
  }

  public DeltaItemLinkTypes setOfficer(DeltaOfficerLinkTypes officer) {
    this.officer = officer;
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
    DeltaItemLinkTypes itemLinkTypes = (DeltaItemLinkTypes) o;
    return Objects.equals(this.self, itemLinkTypes.self) &&
        Objects.equals(this.officer, itemLinkTypes.officer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, officer);
  }

  @Override
  public String toString() {
    return "class ItemLinkTypes {\n"
            + "    self: " + toIndentedString(self) + "\n"
            + "    officer: " + toIndentedString(officer) + "\n"
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
