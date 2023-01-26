package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "delta_appointments")
public class DeltaAppointmentApiEntity extends DeltaAppointmentApi {

    public DeltaAppointmentApiEntity(final DeltaAppointmentApi api){
        super(api.getId(), api.getEtag(), api.getData(), api.getSensitiveData(), api.getInternalData(), api.getInternalId(),
                api.getAppointmentId(), api.getOfficerId(), api.getPreviousOfficerId(), api.getCompanyNumber(), api.getUpdatedAt(), api.getCreated(),
                api.getDeltaAt(), api.getOfficerRoleSortOrder());
    }

    public DeltaAppointmentApiEntity(){}
}
