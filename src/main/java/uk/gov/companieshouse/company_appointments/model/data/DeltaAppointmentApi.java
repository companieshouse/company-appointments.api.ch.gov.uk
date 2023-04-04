package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

public class DeltaAppointmentApi {
    @Id
    private String id;
    @Field("etag")
    private String etag;
    @Field("data")
    private DeltaOfficerData data;
    @Field("sensitive_data")
    private SensitiveData sensitiveData;
    @Field("internal_id")
    private String internalId;
    @Field("appointment_id")
    private String appointmentId;
    @Field("officer_id")
    private String officerId;
    @Field("previous_officer_id")
    private String previousOfficerId;
    @Field("company_number")
    private String companyNumber;
    @Field("updated")
    private InstantAPI updated;
    @Field("updated_by")
    private String updatedBy;
    @Field("created")
    private InstantAPI created;
    @Field("delta_at")
    private String deltaAt;
    @Field("officer_role_sort_order")
    private int officerRoleSortOrder;

    public DeltaAppointmentApi() {
    }

    public DeltaAppointmentApi(String id, String etag, DeltaOfficerData data, SensitiveData sensitiveData, String internalId,
                               String appointmentId, String officerId, String previousOfficerId, String companyNumber,
                               InstantAPI updated, String updatedBy, InstantAPI created, String deltaAt, int officerRoleSortOrder) {
        this.id = id;
        this.etag = etag;
        this.data = data;
        this.sensitiveData = sensitiveData;
        this.internalId = internalId;
        this.appointmentId = appointmentId;
        this.officerId = officerId;
        this.previousOfficerId = previousOfficerId;
        this.companyNumber = companyNumber;
        this.updated = updated;
        this.updatedBy = updatedBy;
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

    public DeltaOfficerData getData() {
        return data;
    }

    public void setData(DeltaOfficerData data) {
        this.data = data;
    }

    public SensitiveData getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(SensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
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

    public InstantAPI getUpdated() {
        return updated;
    }

    public void setUpdatedAt(InstantAPI updated) {
        this.updated = updated;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeltaAppointmentApi that = (DeltaAppointmentApi) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getEtag(), that.getEtag())
                && Objects.equals(getData(), that.getData())
                && Objects.equals(getSensitiveData(), that.getSensitiveData())
                && Objects.equals(getInternalId(), that.getInternalId())
                && Objects.equals(getAppointmentId(), that.getAppointmentId())
                && Objects.equals(getOfficerId(), that.getOfficerId())
                && Objects.equals(getPreviousOfficerId(), that.getPreviousOfficerId())
                && Objects.equals(getCompanyNumber(), that.getCompanyNumber())
                && Objects.equals(getUpdated(), that.getUpdated())
                && Objects.equals(getDeltaAt(), that.getDeltaAt())
                && Objects.equals(getCreated(), that.getCreated())
                && Objects.equals(getUpdatedBy(), that.getUpdatedBy())
                && Objects.equals(getOfficerRoleSortOrder(), that.getOfficerRoleSortOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEtag(), getData(), getSensitiveData(), getInternalId(), getAppointmentId(),
                getOfficerId(), getPreviousOfficerId(), getCompanyNumber(),
                getUpdated(), getCreated(), getDeltaAt(), getOfficerRoleSortOrder());
    }

    @Override
    public String toString() {
        return "DeltaAppointmentApi{" +
                "id='" + id + '\'' +
                ", etag='" + etag + '\'' +
                ", data=" + data +
                ", sensitiveData=" + sensitiveData +
                ", internalId='" + internalId + '\'' +
                ", appointmentId='" + appointmentId + '\'' +
                ", officerId='" + officerId + '\'' +
                ", previousOfficerId='" + previousOfficerId + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", updated=" + updated +
                ", updatedBy='" + updatedBy + '\'' +
                ", created=" + created +
                ", deltaAt='" + deltaAt + '\'' +
                ", officerRoleSortOrder=" + officerRoleSortOrder +
                '}';
    }
}