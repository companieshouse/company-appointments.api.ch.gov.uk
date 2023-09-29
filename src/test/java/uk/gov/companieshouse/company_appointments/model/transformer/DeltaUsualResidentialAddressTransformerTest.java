package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaUsualResidentialAddress;

@ExtendWith(MockitoExtension.class)
class DeltaUsualResidentialAddressTransformerTest {

    private final DeltaUsualResidentialAddressTransformer transformer = new DeltaUsualResidentialAddressTransformer();

    @Test
    void shouldTransformUsualResidentialAddress() throws FailedToTransformException {
        // given
        UsualResidentialAddress source = new UsualResidentialAddress()
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .careOf("care of")
                .country("country")
                .locality("locality")
                .poBox("po box")
                .postalCode("postal code")
                .premises("premises")
                .region("region");

        DeltaUsualResidentialAddress expected = new DeltaUsualResidentialAddress()
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
        DeltaUsualResidentialAddress actual = transformer.transform(source);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldTransformUsualResidentialAddressNulls() throws FailedToTransformException {
        // given
        // when
        DeltaUsualResidentialAddress actual = transformer.transform(new UsualResidentialAddress());

        // then
        assertThat(actual).isNull();

    }
}