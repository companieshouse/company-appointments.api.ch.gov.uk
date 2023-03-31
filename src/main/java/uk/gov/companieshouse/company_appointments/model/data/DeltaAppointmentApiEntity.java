package uk.gov.companieshouse.company_appointments.model.data;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;

@Document(collection = "delta_appointments")
public class DeltaAppointmentApiEntity extends DeltaAppointmentApi {

    private String companyName;
    private String companyStatus;
    private String etag;
    private InstantAPI updatedAt;


    public DeltaAppointmentApiEntity(final DeltaAppointmentApi api){
        super(api.getId(), api.getEtag(), api.getData(), api.getSensitiveData(), api.getInternalId(),
                api.getAppointmentId(), api.getOfficerId(), api.getPreviousOfficerId(), api.getCompanyNumber(), api.getUpdatedAt(),
                api.getUpdatedBy(), api.getCreated(), api.getDeltaAt(), api.getOfficerRoleSortOrder());
    }

    public DeltaAppointmentApiEntity(final DeltaAppointmentApi api, String companyName, String companyStatus){
        super(api.getId(), api.getEtag(), api.getData(), api.getSensitiveData(), api.getInternalId(),
                api.getAppointmentId(), api.getOfficerId(), api.getPreviousOfficerId(), api.getCompanyNumber(), api.getUpdatedAt(),
                api.getUpdatedBy(), api.getCreated(), api.getDeltaAt(), api.getOfficerRoleSortOrder());
        this.companyName = companyName;
        this.companyStatus = companyStatus;
    }

    public DeltaAppointmentApiEntity(){}

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

    @Override
    public void setEtag(String etag) {
        this.etag = etag;
    }
    @Override
    public void setUpdatedAt(InstantAPI updatedAt) {
        this.updatedAt = updatedAt;
    }
}
