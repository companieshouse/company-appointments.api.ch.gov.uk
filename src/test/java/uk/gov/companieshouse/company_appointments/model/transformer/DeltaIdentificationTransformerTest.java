package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum.UK_LIMITED;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;

class DeltaIdentificationTransformerTest {

    private final DeltaIdentificationTransformer transformer = new DeltaIdentificationTransformer();

    @Test
    void shouldTransformValidIdentification() throws FailedToTransformException {
        Identification identification = new Identification()
                .identificationType(UK_LIMITED)
                .legalAuthority("legalAuthority")
                .legalForm("legalForm")
                .placeRegistered("placeRegistered")
                .registrationNumber("registrationNumber");

        DeltaIdentification result = transformer.transform(identification);

        // TODO
        //assertThat()
    }

    @Test
    void shouldTransformWithNullIdentificationType() {
        fail();
    }

    @Test
    void shouldTransformWithInvalidIdentificationType() {
        fail();
    }
}