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
    private DeltaSensitiveData sensitiveData;
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

    public DeltaAppointmentApi(String id, String etag, DeltaOfficerData data, DeltaSensitiveData sensitiveData, String internalId,
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

    public DeltaAppointmentApi setId(String id) {
        this.id = id;
        return this;
    }

    public String getEtag() {
        return etag;
    }

    public DeltaAppointmentApi setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    public DeltaOfficerData getData() {
        return data;
    }

    public DeltaAppointmentApi setData(
            DeltaOfficerData data) {
        this.data = data;
        return this;
    }

    public DeltaSensitiveData getSensitiveData() {
        return sensitiveData;
    }

    public DeltaAppointmentApi setSensitiveData(
            DeltaSensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
        return this;
    }

    public String getInternalId() {
        return internalId;
    }

    public DeltaAppointmentApi setInternalId(String internalId) {
        this.internalId = internalId;
        return this;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public DeltaAppointmentApi setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
        return this;
    }

    public String getOfficerId() {
        return officerId;
    }

    public DeltaAppointmentApi setOfficerId(String officerId) {
        this.officerId = officerId;
        return this;
    }

    public String getPreviousOfficerId() {
        return previousOfficerId;
    }

    public DeltaAppointmentApi setPreviousOfficerId(String previousOfficerId) {
        this.previousOfficerId = previousOfficerId;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public DeltaAppointmentApi setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public InstantAPI getUpdated() {
        return updated;
    }

    public DeltaAppointmentApi setUpdated(
            InstantAPI updated) {
        this.updated = updated;
        return this;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public DeltaAppointmentApi setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public InstantAPI getCreated() {
        return created;
    }

    public DeltaAppointmentApi setCreated(
            InstantAPI created) {
        this.created = created;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public DeltaAppointmentApi setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public int getOfficerRoleSortOrder() {
        return officerRoleSortOrder;
    }

    public DeltaAppointmentApi setOfficerRoleSortOrder(int officerRoleSortOrder) {
        this.officerRoleSortOrder = officerRoleSortOrder;
        return this;
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