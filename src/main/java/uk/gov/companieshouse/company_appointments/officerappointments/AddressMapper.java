package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.company_appointments.model.data.ServiceAddressData;

@Component
class AddressMapper {

    Address map(ServiceAddressData addressData) {
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
