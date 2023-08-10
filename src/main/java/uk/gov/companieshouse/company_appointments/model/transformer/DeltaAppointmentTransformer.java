package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.service.AddCompanyNameAndStatus;

@Component
public class DeltaAppointmentTransformer implements Transformative<FullRecordCompanyOfficerApi, CompanyAppointmentDocument> {

    private final DeltaOfficerDataTransformer officerDataTransformer;
    private final DeltaSensitiveDataTransformer sensitiveDataTransformer;


    public DeltaAppointmentTransformer(DeltaOfficerDataTransformer officerDataTransformer,
            DeltaSensitiveDataTransformer sensitiveDataTransformer) {
        this.officerDataTransformer = officerDataTransformer;
        this.sensitiveDataTransformer = sensitiveDataTransformer;
    }

    @Override
    public CompanyAppointmentDocument factory() {
        return new CompanyAppointmentDocument();
    }

    @Override
    @AddCompanyNameAndStatus
    public CompanyAppointmentDocument transform(FullRecordCompanyOfficerApi source) throws FailedToTransformException {
        return transform(source, factory());
    }

    @Override
    @AddCompanyNameAndStatus
    public CompanyAppointmentDocument transform(FullRecordCompanyOfficerApi api, CompanyAppointmentDocument entity) throws FailedToTransformException {
        try {
            ExternalData externalData = api.getExternalData();
            InternalData internalData = api.getInternalData();

            return entity.data(officerDataTransformer.transform(externalData.getData()))
                    .sensitiveData(externalData.getSensitiveData() != null ?
                            sensitiveDataTransformer.transform(externalData.getSensitiveData()) : null)
                    .id(externalData.getAppointmentId())
                    .internalId(externalData.getInternalId())
                    .appointmentId(externalData.getAppointmentId())
                    .officerId(externalData.getOfficerId())
                    .previousOfficerId(externalData.getPreviousOfficerId())
                    .companyNumber(externalData.getCompanyNumber())
                    .deltaAt(internalData.getDeltaAt().toInstant())
                    .updatedBy(internalData.getUpdatedBy())
                    .officerRoleSortOrder(internalData.getOfficerRoleSortOrder());
        } catch(Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform API payload: %s", e.getMessage()));
        }
    }

}
