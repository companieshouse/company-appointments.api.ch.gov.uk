package uk.gov.companieshouse.company_appointments.model.data;

public record ResourceChangedRequest(String contextId, String companyNumber, String appointmentId, Object officersData,
                                     Boolean isDelete) {

}
