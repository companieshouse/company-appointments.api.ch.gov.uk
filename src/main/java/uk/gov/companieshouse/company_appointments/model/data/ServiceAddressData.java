package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

public class ServiceAddressData {

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

    public ServiceAddressData(String addressLine1, String addressLine2, String careOf, String country, String locality, String poBox, String postcode, String premises, String region) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.careOf = careOf;
        this.country = country;
        this.locality = locality;
        this.poBox = poBox;
        this.postcode = postcode;
        this.premises = premises;
        this.region = region;
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

        public ServiceAddressData build() {
            return new ServiceAddressData(addressLine1, addressLine2, careOf, country, locality, poBox, postcode, premises, region);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceAddressData)) return false;
        ServiceAddressData that = (ServiceAddressData) o;
        return Objects.equals(getAddressLine1(), that.getAddressLine1()) &&
                Objects.equals(getAddressLine2(), that.getAddressLine2()) &&
                Objects.equals(getCareOf(), that.getCareOf()) &&
                Objects.equals(getCountry(), that.getCountry()) &&
                Objects.equals(getLocality(), that.getLocality()) &&
                Objects.equals(getPoBox(), that.getPoBox()) &&
                Objects.equals(getPostcode(), that.getPostcode()) &&
                Objects.equals(getPremises(), that.getPremises()) &&
                Objects.equals(getRegion(), that.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddressLine1(), getAddressLine2(), getCareOf(), getCountry(), getLocality(), getPoBox(), getPostcode(), getPremises(), getRegion());
    }
}
