package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class ContactDetailsData {

    @Field("address_line_1")
    private String addressLine1;

    @Field("address_line_2")
    private String addressLine2;

    @Field("care_of")
    private String careOf;

    @Field("country")
    private String country;

    private String locality;

    @Field("po_box")
    private String poBox;

    @Field("postal_code")
    private String postcode;

    private String premises;

    private String region;

    private String forename;

    @Field("other_forenames")
    private String otherForenames;

    private String surname;

    public ContactDetailsData(String addressLine1, String addressLine2, String careOf,
            String country, String locality, String poBox, String postcode, String premises,
            String region, String forename, String otherForenames, String surname) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.careOf = careOf;
        this.country = country;
        this.locality = locality;
        this.poBox = poBox;
        this.postcode = postcode;
        this.premises = premises;
        this.region = region;
        this.forename = forename;
        this.otherForenames = otherForenames;
        this.surname = surname;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCareOf() {
        return careOf;
    }

    public void setCareOf(String careOf) {
        this.careOf = careOf;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPremises() {
        return premises;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getOtherForenames() {
        return otherForenames;
    }

    public void setOtherForenames(String otherForenames) {
        this.otherForenames = otherForenames;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String addressLine1;
        private String addressLine2;
        private String careOf;
        private String country;
        private String locality;
        private String poBox;
        private String postcode;
        private String premises;
        private String region;
        private String forename;
        private String otherForenames;
        private String surname;

        public Builder withAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
            return this;
        }

        public Builder withAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public Builder withCareOf(String careOf) {
            this.careOf = careOf;
            return this;
        }

        public Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder withLocality(String locality) {
            this.locality = locality;
            return this;
        }

        public Builder withPoBox(String poBox) {
            this.poBox = poBox;
            return this;
        }

        public Builder withPostcode(String postcode) {
            this.postcode = postcode;
            return this;
        }

        public Builder withPremises(String premises) {
            this.premises = premises;
            return this;
        }

        public Builder withRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder withForename(String forename) {
            this.forename = forename;
            return this;
        }

        public Builder withOtherForenames(String otherForenames) {
            this.otherForenames = otherForenames;
            return this;
        }

        public Builder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public ContactDetailsData build() {
            return new ContactDetailsData(addressLine1, addressLine2, careOf, country, locality, poBox,
                    postcode, premises, region, forename, otherForenames, surname);
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
        return Objects.equals(addressLine1, that.addressLine1) &&
                Objects.equals(addressLine2, that.addressLine2) &&
                Objects.equals(careOf, that.careOf) &&
                Objects.equals(country, that.country) &&
                Objects.equals(locality, that.locality) &&
                Objects.equals(poBox, that.poBox) &&
                Objects.equals(postcode, that.postcode) &&
                Objects.equals(premises, that.premises) &&
                Objects.equals(region, that.region) &&
                Objects.equals(forename, that.forename) &&
                Objects.equals(otherForenames, that.otherForenames) &&
                Objects.equals(surname, that.surname);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(addressLine1, addressLine2, careOf, country, locality, poBox, postcode,
                        premises, region, forename, otherForenames, surname);
    }
}
