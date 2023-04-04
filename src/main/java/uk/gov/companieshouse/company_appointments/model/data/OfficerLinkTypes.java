package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import javax.validation.constraints.*;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * OfficerLinkTypes
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class OfficerLinkTypes   {
  @Field("self")
  private String self;

  @Field("appointments")
  private String appointments;

  public OfficerLinkTypes self(String self) {
    this.self = self;
    return this;
  }

  public String getSelf() {
    return self;
  }

  public void setSelf(String self) {
    this.self = self;
  }

  public OfficerLinkTypes appointments(String appointments) {
    this.appointments = appointments;
    return this;
  }

  public String getAppointments() {
    return appointments;
  }

  public void setAppointments(String appointments) {
    this.appointments = appointments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OfficerLinkTypes officerLinkTypes = (OfficerLinkTypes) o;
    return Objects.equals(this.self, officerLinkTypes.self) &&
        Objects.equals(this.appointments, officerLinkTypes.appointments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, appointments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OfficerLinkTypes {\n");
    
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    appointments: ").append(toIndentedString(appointments)).append("\n");
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

