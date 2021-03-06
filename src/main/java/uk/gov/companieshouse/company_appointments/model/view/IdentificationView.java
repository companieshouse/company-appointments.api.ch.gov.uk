package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentificationView {

    @JsonProperty("identification_type")
    private String identificationType;

    @JsonProperty("legal_authority")
    private String legalAuthority;

    @JsonProperty("legal_form")
    private String legalForm;

    @JsonProperty("place_registered")
    private String placeRegistered;

    @JsonProperty("registration_number")
    private String registrationNumber;

    public IdentificationView(String identificationType, String legalAuthority, String legalForm, String placeRegistered, String registrationNumber) {
        this.identificationType = identificationType;
        this.legalAuthority = legalAuthority;
        this.legalForm = legalForm;
        this.placeRegistered = placeRegistered;
        this.registrationNumber = registrationNumber;
    }

    public String getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(String identificationType) {
        this.identificationType = identificationType;
    }

    public String getLegalAuthority() {
        return legalAuthority;
    }

    public void setLegalAuthority(String legalAuthority) {
        this.legalAuthority = legalAuthority;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public String getPlaceRegistered() {
        return placeRegistered;
    }

    public void setPlaceRegistered(String placeRegistered) {
        this.placeRegistered = placeRegistered;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String identificationType;
        private String legalAuthority;
        private String legalForm;
        private String placeRegistered;
        private String registrationNumber;

        public Builder withIdentificationType(String identificationType) {
            this.identificationType = identificationType;
            return this;
        }

        public Builder withLegalAuthority(String legalAuthority) {
            this.legalAuthority = legalAuthority;
            return this;
        }

        public Builder withLegalForm(String legalForm) {
            this.legalForm = legalForm;
            return this;
        }

        public Builder withPlaceRegistered(String placeRegistered) {
            this.placeRegistered = placeRegistered;
            return this;
        }

        public Builder withRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
            return this;
        }

        public IdentificationView build() {
            return new IdentificationView(identificationType, legalAuthority, legalForm, placeRegistered, registrationNumber);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentificationView)) return false;
        IdentificationView that = (IdentificationView) o;
        return Objects.equals(getIdentificationType(), that.getIdentificationType()) &&
                Objects.equals(getLegalAuthority(), that.getLegalAuthority()) &&
                Objects.equals(getLegalForm(), that.getLegalForm()) &&
                Objects.equals(getPlaceRegistered(), that.getPlaceRegistered()) &&
                Objects.equals(getRegistrationNumber(), that.getRegistrationNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentificationType(), getLegalAuthority(), getLegalForm(), getPlaceRegistered(), getRegistrationNumber());
    }
}
