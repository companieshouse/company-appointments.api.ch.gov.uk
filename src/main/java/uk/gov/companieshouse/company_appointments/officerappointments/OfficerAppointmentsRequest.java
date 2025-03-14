package uk.gov.companieshouse.company_appointments.officerappointments;

record OfficerAppointmentsRequest(String officerId, String filter, Integer startIndex, Integer itemsPerPage,
                                  String authPrivileges) {

    OfficerAppointmentsRequest(String officerId, String filter, Integer startIndex, Integer itemsPerPage) {
        this(officerId, filter, startIndex, itemsPerPage, null);
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {

        private String officerId;
        private String filter;
        private Integer startIndex;
        private Integer itemsPerPage;
        private String authPrivileges;

        private Builder() {
        }

        Builder officerId(String officerId) {
            this.officerId = officerId;
            return this;
        }

        Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        Builder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        Builder itemsPerPage(Integer itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        Builder authPrivileges(String authPrivileges) {
            this.authPrivileges = authPrivileges;
            return this;
        }

        OfficerAppointmentsRequest build() {
            return new OfficerAppointmentsRequest(officerId, filter, startIndex, itemsPerPage, authPrivileges);
        }
    }
}
