package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;

@Component
class ContactDetailsMapper {

    ContactDetails map(ContactDetailsData contactDetailsData) {
        return ofNullable(contactDetailsData)
                .map(contactDetails -> new ContactDetails()
                        .contactName(contactDetails.getContactName()))
                .orElse(null);
    }
}
