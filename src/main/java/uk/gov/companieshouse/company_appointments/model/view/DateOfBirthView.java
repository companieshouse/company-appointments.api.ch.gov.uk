package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DateOfBirthView {

    private Integer day;
    private Integer month;
    private Integer year;

    public DateOfBirthView(Integer day, Integer month, Integer year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public DateOfBirthView() {}

    public Integer getDay() {
        return day;
    }


    public Integer getMonth() {
        return month;
    }


    public Integer getYear() {
        return year;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateOfBirthView that)) return false;
        return Objects.equals(getDay(), that.getDay()) &&
                Objects.equals(getMonth(), that.getMonth()) &&
                Objects.equals(getYear(), that.getYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDay(), getMonth(), getYear());
    }

    @Override
    public String toString() {
        return "DateOfBirthView{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
