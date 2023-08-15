package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;

class ContactDetailsMapperTest {

    private ContactDetailsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ContactDetailsMapper();
    }

    @Test
    void mapContactDetails() {
        // given
        DeltaContactDetails contactDetails = new DeltaContactDetails()
                .setContactName("contactName");

        ContactDetails expected = new ContactDetails()
                .contactName("contactName");
        // when
        ContactDetails actual = mapper.map(contactDetails);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapContactDetailsNullContactName() {
        // given
        DeltaContactDetails contactDetails = new DeltaContactDetails();

        ContactDetails expected = new ContactDetails();
        // when
        ContactDetails actual = mapper.map(contactDetails);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapContactDetailsNull() {
        // given
        // when
        ContactDetails actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}