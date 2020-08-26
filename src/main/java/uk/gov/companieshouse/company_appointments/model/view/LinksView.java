package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinksView {

    private String self;
    private OfficerLinksView officer;

    public LinksView(String self, OfficerLinksView officer) {
        this.self = self;
        this.officer = officer;
    }

    public LinksView(String self, String officerAppointments){
        this.self = self;
        this.officer = new OfficerLinksView(officerAppointments);
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public OfficerLinksView getOfficer() {
        return officer;
    }

    public void setOfficer(OfficerLinksView officer) {
        this.officer = officer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinksView)) return false;
        LinksView linksView = (LinksView) o;
        return Objects.equals(getSelf(), linksView.getSelf()) &&
                Objects.equals(getOfficer(), linksView.getOfficer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSelf(), getOfficer());
    }
}
