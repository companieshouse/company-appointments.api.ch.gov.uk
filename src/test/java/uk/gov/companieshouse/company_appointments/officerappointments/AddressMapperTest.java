package uk.gov.companieshouse.company_appointments.officerappointments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;

class AddressMapperTest {

    private AddressMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AddressMapper();
    }

    @Test
    void mapServiceAddress() {
        // given
        DeltaServiceAddress addressData = new DeltaServiceAddress()
                .setAddressLine1("1 Crown Way")
                .setAddressLine2("Pavement")
                .setCareOf("careOf")
                .setCountry("UK")
                .setLocality("Cardiff")
                .setPoBox("poBox")
                .setPostalCode("CF14 3UZ")
                .setPremises("premises")
                .setRegion("Cardiff");

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
        Address actual = mapper.map(addressData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapPrincipalOfficeAddress() {
        // given
        DeltaPrincipalOfficeAddress addressData = new DeltaPrincipalOfficeAddress()
                .setAddressLine1("1 Crown Way")
                .setAddressLine2("Pavement")
                .setCareOf("careOf")
                .setCountry("UK")
                .setLocality("Cardiff")
                .setPoBox("poBox")
                .setPostalCode("CF14 3UZ")
                .setPremises("premises")
                .setRegion("Cardiff");

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
        Address actual = mapper.map(addressData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapServiceAddressNull() {
        // given
        DeltaServiceAddress addressData = new DeltaServiceAddress();
        Address expected = new Address();

        // when
        Address actual = mapper.map(addressData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapPrincipalOfficeAddressNull() {
        // given
        DeltaPrincipalOfficeAddress addressData = new DeltaPrincipalOfficeAddress();
        Address expected = new Address();

        // when
        Address actual = mapper.map(addressData);

        // then
        assertEquals(expected, actual);
    }
}