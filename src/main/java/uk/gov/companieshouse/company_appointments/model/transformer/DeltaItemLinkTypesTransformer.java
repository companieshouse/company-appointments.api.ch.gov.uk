package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;

@Component
public class DeltaItemLinkTypesTransformer implements Transformative<ItemLinkTypes, DeltaItemLinkTypes> {
    @Override
    public DeltaItemLinkTypes factory() {
        return new DeltaItemLinkTypes();
    }

    @Override
    public DeltaItemLinkTypes transform(ItemLinkTypes source, DeltaItemLinkTypes entity) throws FailedToTransformException {

        try {
            entity.setSelf(source.getSelf());
            entity.setOfficer(new DeltaOfficerLinkTypes()
                    .self(source.getOfficer().getSelf())
                    .appointments(source.getOfficer().getAppointments()));

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform DeltaItemLinkTypes: %s", e.getMessage()));
        }

    }
}
