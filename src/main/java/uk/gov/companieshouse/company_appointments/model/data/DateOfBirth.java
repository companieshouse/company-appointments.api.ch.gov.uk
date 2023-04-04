package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;

public class DateOfBirth   {
  private Integer day;

  private Integer month;

  private Integer year;

  public DateOfBirth day(Integer day) {
    this.day = day;
    return this;
  }

  public Integer getDay() {
    return day;
  }

  public void setDay(Integer day) {
    this.day = day;
  }

  public DateOfBirth month(Integer month) {
    this.month = month;
    return this;
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

  public DateOfBirth year(Integer year) {
    this.year = year;
    return this;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DateOfBirth dateOfBirth = (DateOfBirth) o;
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

