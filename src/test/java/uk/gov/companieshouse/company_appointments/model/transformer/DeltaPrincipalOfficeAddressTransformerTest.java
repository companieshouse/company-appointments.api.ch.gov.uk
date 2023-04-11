package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;

@ExtendWith(MockitoExtension.class)
class DeltaPrincipalOfficeAddressTransformerTest {

    private final DeltaPrincipalOfficeAddressTransformer transformer = new DeltaPrincipalOfficeAddressTransformer();

    @Test
    void shouldTransformPrincipalOfficeAddress() throws FailedToTransformException {
        // given
        PrincipalOfficeAddress source = new PrincipalOfficeAddress()
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .careOf("care of")
                .country("country")
                .locality("locality")
                .poBox("po box")
                .postalCode("postal code")
                .premises("premises")
                .region("region");

        DeltaPrincipalOfficeAddress expected = new DeltaPrincipalOfficeAddress()
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
        DeltaPrincipalOfficeAddress actual = transformer.transform(source);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldTransformPrincipalOfficeAddressNulls() throws FailedToTransformException {
        // given
        // when
        DeltaPrincipalOfficeAddress actual = transformer.transform(new PrincipalOfficeAddress());

        // then
        assertThat(actual).isEqualTo(new DeltaPrincipalOfficeAddress());

    }
}