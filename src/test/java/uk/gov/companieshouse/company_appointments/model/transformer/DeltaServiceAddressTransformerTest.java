package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;

@ExtendWith(MockitoExtension.class)
class DeltaServiceAddressTransformerTest {

    private final DeltaServiceAddressTransformer transformer = new DeltaServiceAddressTransformer();

    @Test
    void shouldTransformPrincipalOfficeAddress() throws FailedToTransformException {
        // given
        ServiceAddress source = new ServiceAddress()
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .careOf("care of")
                .country("country")
                .locality("locality")
                .poBox("po box")
                .postalCode("postal code")
                .premises("premises")
                .region("region");

        DeltaServiceAddress expected = new DeltaServiceAddress()
                .setAddressLine1("address line 1")
                .setAddressLine2("address line 2")
                .setCareOf("care of")
                .setCountry("country")
                .setLocality("locality")
                .setPoBox("po box")
                .setPostalCode("postal code")
                .setPremises("premises")
                .setRegion("region");

        // when
        DeltaServiceAddress actual = transformer.transform(source);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldTransformPrincipalOfficeAddressNulls() throws FailedToTransformException {
        // given
        // when
        DeltaServiceAddress actual = transformer.transform(new ServiceAddress());

        // then
        assertThat(actual).isNull();

    }
}