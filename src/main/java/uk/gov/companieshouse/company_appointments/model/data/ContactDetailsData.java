package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class ContactDetailsData {

    @Field("contact_name")
    private String contactName;

    public ContactDetailsData(String contactName) {
        this.contactName = contactName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String contactName;

        public Builder withContactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public ContactDetailsData build() {
            return new ContactDetailsData(contactName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContactDetailsData that = (ContactDetailsData) o;
        return Objects.equals(contactName, that.contactName);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(contactName);
    }
}
