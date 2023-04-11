package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaDateOfBirth;

class DeltaDateOfBirthTransformerTest {

    private final DeltaDateOfBirthTransformer dateOfBirthTransformer = new DeltaDateOfBirthTransformer();

    @Test
    void shouldTransformDateOfBirthToDeltaDateOfBirth() throws FailedToTransformException {
        DateOfBirth dateOfBirth = new DateOfBirth()
                .day(1)
                .month(2)
                .year(2001);

        DeltaDateOfBirth result = dateOfBirthTransformer.transform(dateOfBirth);

        assertThat(result.getDay()).isEqualTo(dateOfBirth.getDay());
        assertThat(result.getMonth()).isEqualTo(dateOfBirth.getMonth());
        assertThat(result.getYear()).isEqualTo(dateOfBirth.getYear());
    }

    @Test
    void shouldTransformMissingValuesToNull() throws FailedToTransformException {
        DateOfBirth dateOfBirth = new DateOfBirth()
                .month(2)
                .year(2001);

        DeltaDateOfBirth result = dateOfBirthTransformer.transform(dateOfBirth);

        assertThat(result.getDay()).isEqualTo(dateOfBirth.getDay());
        assertThat(result.getMonth()).isEqualTo(dateOfBirth.getMonth());
        assertThat(result.getYear()).isEqualTo(dateOfBirth.getYear());
    }
}