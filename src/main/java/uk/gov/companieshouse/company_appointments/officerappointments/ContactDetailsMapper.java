package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;

public class ContactDetailsMapper {

    private ContactDetailsMapper() {
    }

    protected static ContactDetails mapContactDetails(ContactDetailsData contactDetailsData) {
        return ofNullable(contactDetailsData)
                .map(contactDetails -> new ContactDetails()
                        .contactName(contactDetails.getContactName()))
                .orElse(null);
    }
}
