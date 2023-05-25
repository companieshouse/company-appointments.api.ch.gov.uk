package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormerNamesView {

    private String forenames;
    private String surname;

    public FormerNamesView(String forenames, String surname) {
        this.forenames = forenames;
        this.surname = surname;
    }

    public String getForenames() {
        return forenames;
    }

    public void setForenames(String forenames) {
        this.forenames = forenames;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormerNamesView)) return false;
        FormerNamesView that = (FormerNamesView) o;
        return Objects.equals(getForenames(), that.getForenames()) &&
                Objects.equals(getSurname(), that.getSurname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForenames(), getSurname());
    }

}
