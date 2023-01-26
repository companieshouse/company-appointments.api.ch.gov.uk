package uk.gov.companieshouse.company_appointments.model.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.api.appointment.*;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.InstantAPI;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeltaAppointmentApi {

    @JsonProperty("id")
    private String id;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("data")
    private Data data;

    @JsonProperty("sensitive_data")
    private SensitiveData sensitiveData;

    @JsonProperty("internal_data")
    private InternalData internalData;

    @JsonProperty("internal_id")
    private String internalId;

    @JsonProperty("appointment_id")
    private String appointmentId;

    @JsonProperty("officer_id")
    private String officerId;

    @JsonProperty("previous_officer_id")
    private String previousOfficerId;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("updated_at")
    private InstantAPI updatedAt;

    @JsonProperty("created")
    private InstantAPI created;

    @JsonProperty("delta_at")
    private String deltaAt;
    @JsonProperty("officer_role_sort_order")
    private int officerRoleSortOrder;

    public DeltaAppointmentApi() {
    }

    public DeltaAppointmentApi(final FullRecordCompanyOfficerApi api){super();}

    public DeltaAppointmentApi(String id, String etag, Data data, SensitiveData sensitiveData, InternalData internalData, String internalId,
                               String appointmentId, String officerId, String previousOfficerId, String companyNumber,
                               InstantAPI updatedAt, InstantAPI created, String deltaAt, int officerRoleSortOrder) {
        this.id = id;
        this.etag = etag;
        this.data = data;
        this.sensitiveData = sensitiveData;
        this.internalData = internalData;
        this.internalId = internalId;
        this.appointmentId = appointmentId;
        this.officerId = officerId;
        this.previousOfficerId = previousOfficerId;
        this.companyNumber = companyNumber;
        this.updatedAt = updatedAt;
        this.created = created;
        this.deltaAt = deltaAt;
        this.officerRoleSortOrder = officerRoleSortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public SensitiveData getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(SensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
    }

    public InternalData getInternalData(){
        return internalData;
    }

    public void setInternalData(InternalData internalData) {
        this.internalData = internalData;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getPreviousOfficerId() {
        return previousOfficerId;
    }

    public void setPreviousOfficerId(String previousOfficerId) {
        this.previousOfficerId = previousOfficerId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public InstantAPI getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(InstantAPI updatedAt) {
        this.updatedAt = updatedAt;
    }

    public InstantAPI getCreated() {
        return created;
    }

    public void setCreated(InstantAPI created) {
        this.created = created;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public void setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
    }

    public int getOfficerRoleSortOrder() {
        return officerRoleSortOrder;
    }

    public void setOfficerRoleSortOrder(int officerRoleSortOrder) {
        this.officerRoleSortOrder = officerRoleSortOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeltaAppointmentApi that = (DeltaAppointmentApi) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getEtag(), that.getEtag())
                && Objects.equals(getData(), that.getData())
                && Objects.equals(getSensitiveData(), that.getSensitiveData())
                && Objects.equals(getInternalData(), that.getInternalData())
                && Objects.equals(getInternalId(), that.getInternalId())
                && Objects.equals(getAppointmentId(), that.getAppointmentId())
                && Objects.equals(getOfficerId(), that.getOfficerId())
                && Objects.equals(getPreviousOfficerId(), that.getPreviousOfficerId())
                && Objects.equals(getCompanyNumber(), that.getCompanyNumber())
                && Objects.equals(getUpdatedAt(), that.getUpdatedAt())
                && Objects.equals(getDeltaAt(), that.getDeltaAt())
                && Objects.equals(getCreated(), that.getCreated())
                && Objects.equals(getOfficerRoleSortOrder(), that.getOfficerRoleSortOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEtag(), getData(), getSensitiveData(), getInternalData(), getInternalId(), getAppointmentId(),
                getOfficerId(), getPreviousOfficerId(), getCompanyNumber(),
                getUpdatedAt(), getCreated(), getDeltaAt(), getOfficerRoleSortOrder());
    }
}
