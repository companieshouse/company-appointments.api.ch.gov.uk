package uk.gov.companieshouse.company_appointments.model.data;

public record ResourceChangedRequest(String contextId, String companyNumber, String appointmentId, Object officersData,
                                     Boolean isDelete) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String contextId;
        private String companyNumber;
        private String appointmentId;
        private Object officerData;
        private Boolean isDelete;

        private Builder() {}

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder appointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder officerData(Object officerData) {
            this.officerData = officerData;
            return this;
        }

        public Builder delete(Boolean delete) {
            isDelete = delete;
            return this;
        }

        public ResourceChangedRequest build() {
            return new ResourceChangedRequest(contextId, companyNumber, appointmentId, officerData, isDelete);
        }
    }
}
