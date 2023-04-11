package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * OfficerLinkTypes
 */
public class DeltaOfficerLinkTypes {
  @Field("self")
  private String self;
  @Field("appointments")
  private String appointments;

  public String getSelf() {
    return self;
  }

  public DeltaOfficerLinkTypes setSelf(String self) {
    this.self = self;
    return this;
  }

  public String getAppointments() {
    return appointments;
  }

  public DeltaOfficerLinkTypes setAppointments(String appointments) {
    this.appointments = appointments;
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
    DeltaOfficerLinkTypes officerLinkTypes = (DeltaOfficerLinkTypes) o;
    return Objects.equals(this.self, officerLinkTypes.self) &&
        Objects.equals(this.appointments, officerLinkTypes.appointments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, appointments);
  }

  @Override
  public String toString() {
    return "class OfficerLinkTypes {\n"
            + "    self: " + toIndentedString(self) + "\n"
            + "    appointments: " + toIndentedString(appointments) + "\n"
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

