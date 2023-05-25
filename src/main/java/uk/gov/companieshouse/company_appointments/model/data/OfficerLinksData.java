package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class OfficerLinksData {

    @Field("self")
    private String selfLink;

    @Field("appointments")
    private String appointmentsLink;

    public OfficerLinksData(String selfLink, String appointmentsLink) {
        this.selfLink = selfLink;
        this.appointmentsLink = appointmentsLink;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getAppointmentsLink() {
        return appointmentsLink;
    }

    public void setAppointmentsLink(String appointmentsLink) {
        this.appointmentsLink = appointmentsLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfficerLinksData)) return false;
        OfficerLinksData that = (OfficerLinksData) o;
        return Objects.equals(getSelfLink(), that.getSelfLink()) &&
                Objects.equals(getAppointmentsLink(), that.getAppointmentsLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSelfLink(), getAppointmentsLink());
    }
}
