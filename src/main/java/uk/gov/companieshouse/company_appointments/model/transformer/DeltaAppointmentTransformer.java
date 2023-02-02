package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;

@Component
public class DeltaAppointmentTransformer implements Transformative<FullRecordCompanyOfficerApi, DeltaAppointmentApi> {
    @Override
    public DeltaAppointmentApi factory() {
        return new DeltaAppointmentApi();
    }

    public DeltaAppointmentApi transform(FullRecordCompanyOfficerApi api, DeltaAppointmentApi entity) {

        entity.setData(api.getExternalData().getData());
        entity.setSensitiveData(api.getExternalData().getSensitiveData());
        entity.setId(api.getExternalData().getAppointmentId());
        entity.setInternalId(api.getExternalData().getInternalId());
        entity.setAppointmentId(api.getExternalData().getAppointmentId());
        entity.setOfficerId(api.getExternalData().getOfficerId());
        entity.setPreviousOfficerId(api.getExternalData().getPreviousOfficerId());
        entity.setCompanyNumber(api.getExternalData().getCompanyNumber());
        populateInternalFields(entity, api.getInternalData());

        return entity;
    }

    private void populateInternalFields(DeltaAppointmentApi entity, InternalData internalData) {
        entity.setDeltaAt(internalData.getDeltaAt().toString());
        entity.setUpdatedBy(internalData.getUpdatedBy());
        entity.setOfficerRoleSortOrder(internalData.getOfficerRoleSortOrder());
    }

}
