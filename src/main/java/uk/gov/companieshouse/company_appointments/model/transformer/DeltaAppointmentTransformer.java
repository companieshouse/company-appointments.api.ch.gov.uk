package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;

@Component
public class DeltaAppointmentTransformer implements Transformative<FullRecordCompanyOfficerApi, DeltaAppointmentApi> {

    private final DeltaOfficerDataTransformer officerDataTransformer;
    private final DeltaSensitiveDataTransformer sensitiveDataTransformer;


    public DeltaAppointmentTransformer(DeltaOfficerDataTransformer officerDataTransformer,
            DeltaSensitiveDataTransformer sensitiveDataTransformer) {
        this.officerDataTransformer = officerDataTransformer;
        this.sensitiveDataTransformer = sensitiveDataTransformer;
    }

    @Override
    public DeltaAppointmentApi factory() {
        return new DeltaAppointmentApi();
    }

    public DeltaAppointmentApi transform(FullRecordCompanyOfficerApi api, DeltaAppointmentApi entity) throws FailedToTransformException {

        try {
            ExternalData externalData = api.getExternalData();
            entity.setData(officerDataTransformer.transform(externalData.getData()));
            entity.setSensitiveData(externalData.getSensitiveData() != null?
                    sensitiveDataTransformer.transform(externalData.getSensitiveData()) : null);
            entity.setId(externalData.getAppointmentId());
            entity.setInternalId(externalData.getInternalId());
            entity.setAppointmentId(externalData.getAppointmentId());
            entity.setOfficerId(externalData.getOfficerId());
            entity.setPreviousOfficerId(externalData.getPreviousOfficerId());
            entity.setCompanyNumber(externalData.getCompanyNumber());
            populateInternalFields(entity, api.getInternalData());

            return entity;
        } catch(Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform API payload: %s", e.getMessage()));
        }
    }

    private void populateInternalFields(DeltaAppointmentApi entity, InternalData internalData) {
        entity.setDeltaAt(internalData.getDeltaAt().toString());
        entity.setUpdatedBy(internalData.getUpdatedBy());
        entity.setOfficerRoleSortOrder(internalData.getOfficerRoleSortOrder());
    }
}
