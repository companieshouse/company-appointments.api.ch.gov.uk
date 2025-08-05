package uk.gov.companieshouse.company_appointments.kafka;

public interface OfficerMergeProducer {

    void invokeOfficerMerge(String officerId, String previousOfficerId);
}
