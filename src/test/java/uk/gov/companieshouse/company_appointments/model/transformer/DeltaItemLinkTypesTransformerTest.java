package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;

class DeltaItemLinkTypesTransformerTest {

    private final DeltaItemLinkTypesTransformer transformer = new DeltaItemLinkTypesTransformer();

    @Test
    void shouldTransformItemLinkTypes() {

        ItemLinkTypes itemLinkTypes = new ItemLinkTypes()
                .self("self")
                .officer(new OfficerLinkTypes()
                        .self("officer type")
                        .appointments("appointments"));

        DeltaItemLinkTypes result = transformer.transform(itemLinkTypes);

        assertThat(result.getSelf()).isEqualTo("self");
        assertThat(result.getOfficer()).isNotNull();
        assertThat(result.getOfficer().getSelf()).isEqualTo("officer type");
        assertThat(result.getOfficer().getAppointments()).isEqualTo("appointments");
    }

    @Test
    void shouldTransformItemLinkTypesWithNullOfficerLinkTypes() {
        ItemLinkTypes itemLinkTypes = new ItemLinkTypes()
                .self("self");

        DeltaItemLinkTypes result = transformer.transform(itemLinkTypes);

        assertThat(result.getSelf()).isEqualTo("self");
        assertThat(result.getOfficer()).isNull();
    }
}