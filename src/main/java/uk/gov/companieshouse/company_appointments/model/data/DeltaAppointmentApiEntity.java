package uk.gov.companieshouse.company_appointments.model.data;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;

@Document(collection = "delta_appointments")
public class DeltaAppointmentApiEntity extends DeltaAppointmentApi {

    private String companyName;
    private String companyStatus;

    public DeltaAppointmentApiEntity(final DeltaAppointmentApi api){
        super(api.getId(), api.getEtag(), api.getData(), api.getSensitiveData(), api.getInternalId(),
                api.getAppointmentId(), api.getOfficerId(), api.getPreviousOfficerId(), api.getCompanyNumber(), api.getUpdatedAt(),
                api.getUpdatedBy(), api.getCreated(), api.getDeltaAt(), api.getOfficerRoleSortOrder());
    }

    public DeltaAppointmentApiEntity(){}

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

}
