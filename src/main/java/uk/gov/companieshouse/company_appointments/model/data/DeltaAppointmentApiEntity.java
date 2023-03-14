package uk.gov.companieshouse.company_appointments.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;

@Document(collection = "delta_appointments")
public class DeltaAppointmentApiEntity extends DeltaAppointmentApi {

    public DeltaAppointmentApiEntity(final DeltaAppointmentApi api){
        super(api.getId(), api.getEtag(), api.getData(), api.getSensitiveData(), api.getInternalId(),
                api.getAppointmentId(), api.getOfficerId(), api.getPreviousOfficerId(), api.getCompanyNumber(), api.getUpdatedAt(),
                api.getUpdatedBy(), api.getCreated(), api.getDeltaAt(), api.getOfficerRoleSortOrder());
    }

    public DeltaAppointmentApiEntity(){}

    @JsonProperty("_id")
    private String idOther;
}
