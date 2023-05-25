package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class LinksData {

    @Field("officer")
    private OfficerLinksData officerLinksData;

    @Field("self")
    private String selfLink;

    public LinksData(OfficerLinksData officerLinksData, String selfLink) {
        this.officerLinksData = officerLinksData;
        this.selfLink = selfLink;
    }

    public LinksData(String self, String officerSelf, String officerAppointments) {
        this.selfLink = self;
        this.officerLinksData = new OfficerLinksData(officerSelf, officerAppointments);
    }

    public LinksData() {
    }

    public OfficerLinksData getOfficerLinksData() {
        return officerLinksData;
    }

    public void setOfficerLinksData(OfficerLinksData officerLinksData) {
        this.officerLinksData = officerLinksData;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinksData)) return false;
        LinksData linksData = (LinksData) o;
        return Objects.equals(getOfficerLinksData(), linksData.getOfficerLinksData()) &&
                Objects.equals(getSelfLink(), linksData.getSelfLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOfficerLinksData(), getSelfLink());
    }
}
