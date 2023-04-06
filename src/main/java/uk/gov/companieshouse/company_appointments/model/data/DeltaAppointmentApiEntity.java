package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;

@Document(collection = "delta_appointments")
public class DeltaAppointmentApiEntity extends DeltaAppointmentApi {

    @Field("company_name")
    private String companyName;
    @Field("company_status")
    private String companyStatus;

    public DeltaAppointmentApiEntity(final DeltaAppointmentApi api){
        super(api.getId(), api.getEtag(), api.getData(), api.getSensitiveData(), api.getInternalId(),
                api.getAppointmentId(), api.getOfficerId(), api.getPreviousOfficerId(), api.getCompanyNumber(), api.getUpdated(),
                api.getUpdatedBy(), api.getCreated(), api.getDeltaAt(), api.getOfficerRoleSortOrder());
    }

    public DeltaAppointmentApiEntity(){}

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

}
