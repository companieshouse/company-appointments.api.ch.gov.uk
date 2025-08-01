package uk.gov.companieshouse.company_appointments.api;

public interface OfficerMergeClient {

    void invokeOfficerMerge(String officerId, String previousOfficerId);

}
