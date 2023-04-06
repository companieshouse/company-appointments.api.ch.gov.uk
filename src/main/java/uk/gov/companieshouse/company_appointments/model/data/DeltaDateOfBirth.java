package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;

public class DeltaDateOfBirth {
  private Integer day;
  private Integer month;
  private Integer year;

  public Integer getDay() {
    return day;
  }

  public DeltaDateOfBirth setDay(Integer day) {
    this.day = day;
    return this;
  }

  public Integer getMonth() {
    return month;
  }

  public DeltaDateOfBirth setMonth(Integer month) {
    this.month = month;
    return this;
  }

  public Integer getYear() {
    return year;
  }

  public DeltaDateOfBirth setYear(Integer year) {
    this.year = year;
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
    DeltaDateOfBirth dateOfBirth = (DeltaDateOfBirth) o;
    return Objects.equals(this.day, dateOfBirth.day) &&
        Objects.equals(this.month, dateOfBirth.month) &&
        Objects.equals(this.year, dateOfBirth.year);
  }

  @Override
  public int hashCode() {
    return Objects.hash(day, month, year);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DateOfBirth {\n");
    
    sb.append("    day: ").append(toIndentedString(day)).append("\n");
    sb.append("    month: ").append(toIndentedString(month)).append("\n");
    sb.append("    year: ").append(toIndentedString(year)).append("\n");
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

