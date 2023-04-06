package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * FormerNames
 */
public class DeltaFormerNames {
  @Field("forenames")
  private String forenames;
  @Field("surname")
  private String surname;

  public String getForenames() {
    return forenames;
  }

  public DeltaFormerNames setForenames(String forenames) {
    this.forenames = forenames;
    return this;
  }

  public String getSurname() {
    return surname;
  }

  public DeltaFormerNames setSurname(String surname) {
    this.surname = surname;
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
    DeltaFormerNames formerNames = (DeltaFormerNames) o;
    return Objects.equals(this.forenames, formerNames.forenames) &&
        Objects.equals(this.surname, formerNames.surname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(forenames, surname);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FormerNames {\n");
    
    sb.append("    forenames: ").append(toIndentedString(forenames)).append("\n");
    sb.append("    surname: ").append(toIndentedString(surname)).append("\n");
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
