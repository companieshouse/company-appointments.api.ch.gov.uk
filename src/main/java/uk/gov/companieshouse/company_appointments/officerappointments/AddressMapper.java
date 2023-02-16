package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

public class AddressMapper {

    private AddressMapper() {
    }

    public static Address mapAddress(ServiceAddressData addressData) {
        return ofNullable(addressData)
                .map(address -> new Address()
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .careOf(address.getCareOf())
                        .country(address.getCountry())
                        .locality(address.getLocality())
                        .poBox(address.getPoBox())
                        .postalCode(address.getPostcode())
                        .premises(address.getPremises())
                        .region(address.getRegion()))
                .orElse(null);
    }
}
