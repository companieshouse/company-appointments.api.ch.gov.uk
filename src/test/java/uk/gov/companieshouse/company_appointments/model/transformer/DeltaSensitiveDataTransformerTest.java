package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaDateOfBirth;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaUsualResidentialAddress;

@ExtendWith(MockitoExtension.class)
class DeltaSensitiveDataTransformerTest {

    @Mock
    private DeltaUsualResidentialAddressTransformer deltaUsualResidentialAddressTransformer;
    @Mock
    private DeltaDateOfBirthTransformer deltaDateOfBirthTransformer;
    @InjectMocks
    private DeltaSensitiveDataTransformer transformer;

    @Mock
    private UsualResidentialAddress usualResidentialAddress;
    @Mock
    private DateOfBirth dateOfBirth;

    @Mock
    private DeltaUsualResidentialAddress deltaUsualResidentialAddress;
    @Mock
    private DeltaDateOfBirth deltaDateOfBirth;

    @Test
    void shouldTransformSensitiveData() throws FailedToTransformException {
        // given
        when(deltaUsualResidentialAddressTransformer.transform(any(UsualResidentialAddress.class))).thenReturn(deltaUsualResidentialAddress);
        when(deltaDateOfBirthTransformer.transform(any(DateOfBirth.class))).thenReturn(deltaDateOfBirth);

        // when
        DeltaSensitiveData actual = transformer.transform(buildSource());

        // then
        assertThat(actual).isEqualTo(buildExpected());
        verify(deltaUsualResidentialAddressTransformer).transform(usualResidentialAddress);
        verify(deltaDateOfBirthTransformer).transform(dateOfBirth);
    }

    @Test
    void shouldTransformSensitiveDataWithNulls() throws FailedToTransformException {
        // given
        SensitiveData source = buildSource()
                .usualResidentialAddress(null)
                .dateOfBirth(null);

        DeltaSensitiveData expected = buildExpected()
                .setUsualResidentialAddress(null)
                .setDateOfBirth(null);

        // when
        DeltaSensitiveData actual = transformer.transform(source);

        // then
        assertThat(actual).isEqualTo(expected);
        verifyNoInteractions(deltaUsualResidentialAddressTransformer);
        verifyNoInteractions(deltaDateOfBirthTransformer);
    }

    @Test
    void shouldRethrowTransformExceptionWhenCaught() throws FailedToTransformException {
        // given
        when(deltaUsualResidentialAddressTransformer.transform(any(UsualResidentialAddress.class))).thenThrow(new FailedToTransformException("Failed"));

        // when
        Executable executable = () -> transformer.transform(buildSource());

        // then
        FailedToTransformException exception = assertThrows(FailedToTransformException.class, executable);
        assertThat(exception.getMessage()).isEqualTo("Failed to transform SensitiveData: Failed");
    }

    private SensitiveData buildSource() {
        return new SensitiveData()
                .usualResidentialAddress(usualResidentialAddress)
                .dateOfBirth(dateOfBirth)
                .residentialAddressSameAsServiceAddress(true);
    }

    private DeltaSensitiveData buildExpected() {
        return new DeltaSensitiveData()
                .setUsualResidentialAddress(deltaUsualResidentialAddress)
                .setDateOfBirth(deltaDateOfBirth)
                .setResidentialAddressSameAsServiceAddress(true);
    }
}