package uk.gov.companieshouse.company_appointments.model;

public record OfficerMergeMessage(String officerId, String previousOfficerId, String contextId) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String officerId;
        private String previousOfficerId;
        private String contextId;

        public Builder officerId(String officerId) {
            this.officerId = officerId;
            return this;
        }

        public Builder previousOfficerId(String previousOfficerId) {
            this.previousOfficerId = previousOfficerId;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public OfficerMergeMessage build() {
            return new OfficerMergeMessage(officerId, previousOfficerId, contextId);
        }
    }

}
