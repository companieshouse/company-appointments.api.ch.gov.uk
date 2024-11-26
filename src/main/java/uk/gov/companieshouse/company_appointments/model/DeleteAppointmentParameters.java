package uk.gov.companieshouse.company_appointments.model;

public record DeleteAppointmentParameters(String companyNumber, String appointmentId, String deltaAt,
                                          String officerId) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String companyNumber;
        private String appointmentId;
        private String deltaAt;
        private String officerId;

        private Builder() {
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder appointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public Builder officerId(String officerId) {
            this.officerId = officerId;
            return this;
        }

        public DeleteAppointmentParameters build() {
            return new DeleteAppointmentParameters(companyNumber, appointmentId, deltaAt, officerId);
        }
    }
}
