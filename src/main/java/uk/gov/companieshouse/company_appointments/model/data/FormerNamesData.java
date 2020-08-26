package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;

public class FormerNamesData {

    private String forenames;

    private String surname;

    public FormerNamesData(String forenames, String surname) {
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
        if (!(o instanceof FormerNamesData)) return false;
        FormerNamesData that = (FormerNamesData) o;
        return Objects.equals(getForenames(), that.getForenames()) &&
                Objects.equals(getSurname(), that.getSurname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForenames(), getSurname());
    }
}
