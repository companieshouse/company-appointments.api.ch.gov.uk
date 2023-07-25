package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;

class DeltaIdentificationTransformerTest {

    private final DeltaIdentificationTransformer transformer = new DeltaIdentificationTransformer();

    @Test
    void shouldTransformValidIdentification() throws Exception {
        Identification identification = new Identification()
                .identificationType(IdentificationTypeEnum.UK_LIMITED_COMPANY)
                .legalAuthority("legalAuthority")
                .legalForm("legalForm")
                .placeRegistered("placeRegistered")
                .registrationNumber("registrationNumber");

        DeltaIdentification result = transformer.transform(identification);

        assertThat(result.getIdentificationType()).isEqualTo("uk-limited-company");
        assertThat(result.getLegalAuthority()).isEqualTo("legalAuthority");
        assertThat(result.getLegalForm()).isEqualTo("legalForm");
        assertThat(result.getPlaceRegistered()).isEqualTo("placeRegistered");
        assertThat(result.getRegistrationNumber()).isEqualTo("registrationNumber");
    }

    @Test
    void shouldTransformWithNullIdentificationType() throws Exception {
        Identification identification = new Identification()
                .legalAuthority("legalAuthority")
                .legalForm("legalForm")
                .placeRegistered("placeRegistered")
                .registrationNumber("registrationNumber");

        DeltaIdentification result = transformer.transform(identification);

        assertThat(result.getIdentificationType()).isNull();
        assertThat(result.getLegalAuthority()).isEqualTo("legalAuthority");
        assertThat(result.getLegalForm()).isEqualTo("legalForm");
        assertThat(result.getPlaceRegistered()).isEqualTo("placeRegistered");
        assertThat(result.getRegistrationNumber()).isEqualTo("registrationNumber");
    }
}