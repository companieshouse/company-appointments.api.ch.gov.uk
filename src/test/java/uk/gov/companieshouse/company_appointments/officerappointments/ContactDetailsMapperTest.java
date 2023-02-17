package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetailsData;

class ContactDetailsMapperTest {

    private ContactDetailsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ContactDetailsMapper();
    }

    @Test
    void mapContactDetails() {
        // given
        ContactDetailsData contactDetailsData = ContactDetailsData.builder()
                .withContactName("contactName")
                .build();

        ContactDetails expected = new ContactDetails()
                .contactName("contactName");
        // when
        ContactDetails actual = mapper.map(contactDetailsData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapContactDetailsNullContactName() {
        // given
        ContactDetailsData contactDetailsData = ContactDetailsData.builder().build();

        ContactDetails expected = new ContactDetails();
        // when
        ContactDetails actual = mapper.map(contactDetailsData);

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