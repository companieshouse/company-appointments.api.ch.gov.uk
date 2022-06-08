package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class ContactDetailsView {

    @JsonProperty("contact_name")
    private String contactName;

    public ContactDetailsView(String contactName) {
        this.contactName = contactName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public static ContactDetailsView.Builder builder() {
        return new ContactDetailsView.Builder();
    }

    public static final class Builder {

        private String contactName;

        public ContactDetailsView.Builder withContactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public ContactDetailsView build() {
            return new ContactDetailsView(contactName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactDetailsView)) return false;
        ContactDetailsView that = (ContactDetailsView) o;
        return Objects.equals(getContactName(), that.getContactName());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getContactName());
    }
}
