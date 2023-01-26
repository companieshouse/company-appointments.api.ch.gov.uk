package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;

@Component
public class DeltaAppointmentTransformer implements Transformative<FullRecordCompanyOfficerApi, DeltaAppointmentApi> {
    @Override
    public DeltaAppointmentApi factory() {
        return new DeltaAppointmentApi();
    }

    public DeltaAppointmentApi transform(FullRecordCompanyOfficerApi api, DeltaAppointmentApi entity) throws NonRetryableErrorException {

        entity.setData(api.getExternalData().getData());
        entity.setSensitiveData(api.getExternalData().getSensitiveData());
        entity.setInternalId(api.getExternalData().getInternalId());
        entity.setAppointmentId(api.getExternalData().getAppointmentId());
        entity.setOfficerId(api.getExternalData().getOfficerId());
        entity.setPreviousOfficerId(api.getExternalData().getPreviousOfficerId());
        entity.setCompanyNumber(api.getExternalData().getCompanyNumber());
        entity.setOfficerRoleSortOrder(api.getInternalData().getOfficerRoleSortOrder());

        return entity;
    }

}
