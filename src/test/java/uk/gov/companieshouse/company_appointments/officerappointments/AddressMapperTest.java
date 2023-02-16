package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

class AddressMapperTest {

    @Test
    void mapAddress() {
        // given
        ServiceAddressData addressData = ServiceAddressData.builder()
                .withAddressLine1("1 Crown Way")
                .withAddressLine2("Pavement")
                .withCareOf("careOf")
                .withCountry("UK")
                .withLocality("Cardiff")
                .withPoBox("poBox")
                .withPostcode("CF14 3UZ")
                .withPremises("premises")
                .withRegion("Cardiff")
                .build();

        Address expected = new Address()
                .addressLine1("1 Crown Way")
                .addressLine2("Pavement")
                .careOf("careOf")
                .country("UK")
                .locality("Cardiff")
                .poBox("poBox")
                .postalCode("CF14 3UZ")
                .premises("premises")
                .region("Cardiff");

        // when
        Address actual = AddressMapper.mapAddress(addressData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapAddressNull() {
        // given
        ServiceAddressData addressData = ServiceAddressData.builder().build();

        Address expected = new Address();

        // when
        Address actual = AddressMapper.mapAddress(addressData);

        // then
        assertEquals(expected, actual);
    }
}