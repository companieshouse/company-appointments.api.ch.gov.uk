package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerLinksView {

    private String appointments;

    @JsonCreator
    public OfficerLinksView(@JsonProperty("appointments") String appointments) {
        this.appointments = appointments;
    }

    public String getAppointments() {
        return appointments;
    }

    public void setAppointments(String appointments) {
        this.appointments = appointments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfficerLinksView)) return false;
        OfficerLinksView that = (OfficerLinksView) o;
        return Objects.equals(getAppointments(), that.getAppointments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppointments());
    }
}
