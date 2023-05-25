package uk.gov.companieshouse.company_appointments.model.data;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "delta_appointments")
public class CompanyAppointmentDocument {
    @Id
    private String id;
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
    private DeltaTimestamp updated;
    @Field("updated_by")
    private String updatedBy;
    @Field("created")
    private DeltaTimestamp created;
    @Field("delta_at")
    private Instant deltaAt;
    @Field("officer_role_sort_order")
    private int officerRoleSortOrder;
    @Field("company_name")
    private String companyName;
    @Field("company_status")
    private String companyStatus;

    public CompanyAppointmentDocument() {
    }

    private CompanyAppointmentDocument(Builder builder) {
        id = builder.id;
        data = builder.data;
        sensitiveData = builder.sensitiveData;
        internalId = builder.internalId;
        appointmentId = builder.appointmentId;
        officerId = builder.officerId;
        previousOfficerId = builder.previousOfficerId;
        companyNumber = builder.companyNumber;
        updated = builder.updated;
        updatedBy = builder.updatedBy;
        created = builder.created;
        deltaAt = builder.deltaAt;
        officerRoleSortOrder = builder.officerRoleSortOrder;
        companyName = builder.companyName;
        companyStatus = builder.companyStatus;
    }

    public String getId() {
        return id;
    }

    public CompanyAppointmentDocument setId(String id) {
        this.id = id;
        return this;
    }

    public DeltaOfficerData getData() {
        return data;
    }

    public CompanyAppointmentDocument setData(DeltaOfficerData data) {
        this.data = data;
        return this;
    }

    public DeltaSensitiveData getSensitiveData() {
        return sensitiveData;
    }

    public CompanyAppointmentDocument setSensitiveData(DeltaSensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
        return this;
    }

    public String getInternalId() {
        return internalId;
    }

    public CompanyAppointmentDocument setInternalId(String internalId) {
        this.internalId = internalId;
        return this;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public CompanyAppointmentDocument setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
        return this;
    }

    public String getOfficerId() {
        return officerId;
    }

    public CompanyAppointmentDocument setOfficerId(String officerId) {
        this.officerId = officerId;
        return this;
    }

    public String getPreviousOfficerId() {
        return previousOfficerId;
    }

    public CompanyAppointmentDocument setPreviousOfficerId(String previousOfficerId) {
        this.previousOfficerId = previousOfficerId;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public CompanyAppointmentDocument setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public DeltaTimestamp getUpdated() {
        return updated;
    }

    public CompanyAppointmentDocument setUpdated(DeltaTimestamp updated) {
        this.updated = updated;
        return this;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public CompanyAppointmentDocument setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public DeltaTimestamp getCreated() {
        return created;
    }

    public CompanyAppointmentDocument setCreated(DeltaTimestamp created) {
        this.created = created;
        return this;
    }

    public Instant getDeltaAt() {
        return deltaAt;
    }

    public CompanyAppointmentDocument setDeltaAt(Instant deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public int getOfficerRoleSortOrder() {
        return officerRoleSortOrder;
    }

    public CompanyAppointmentDocument setOfficerRoleSortOrder(int officerRoleSortOrder) {
        this.officerRoleSortOrder = officerRoleSortOrder;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public CompanyAppointmentDocument setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public CompanyAppointmentDocument setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
        return this;
    }

    public static final class Builder {
        private String id;
        private DeltaOfficerData data;
        private DeltaSensitiveData sensitiveData;
        private String internalId;
        private String appointmentId;
        private String officerId;
        private String previousOfficerId;
        private String companyNumber;
        private DeltaTimestamp updated;
        private String updatedBy;
        private DeltaTimestamp created;
        private Instant deltaAt;
        private int officerRoleSortOrder;
        private String companyName;
        private String companyStatus;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withData(DeltaOfficerData data) {
            this.data = data;
            return this;
        }

        public Builder withSensitiveData(DeltaSensitiveData sensitiveData) {
            this.sensitiveData = sensitiveData;
            return this;
        }

        public Builder withInternalId(String internalId) {
            this.internalId = internalId;
            return this;
        }

        public Builder withAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder withOfficerId(String officerId) {
            this.officerId = officerId;
            return this;
        }

        public Builder withPreviousOfficerId(String previousOfficerId) {
            this.previousOfficerId = previousOfficerId;
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withUpdated(DeltaTimestamp updated) {
            this.updated = updated;
            return this;
        }

        public Builder withUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder withCreated(DeltaTimestamp created) {
            this.created = created;
            return this;
        }

        public Builder withDeltaAt(Instant deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public Builder withOfficerRoleSortOrder(int officerRoleSortOrder) {
            this.officerRoleSortOrder = officerRoleSortOrder;
            return this;
        }

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder withCompanyStatus(String companyStatus) {
            this.companyStatus = companyStatus;
            return this;
        }

        public CompanyAppointmentDocument build() {
            return new CompanyAppointmentDocument(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompanyAppointmentDocument that = (CompanyAppointmentDocument) o;
        return officerRoleSortOrder == that.officerRoleSortOrder
                && Objects.equals(id, that.id)
                && Objects.equals(data, that.data)
                && Objects.equals(sensitiveData, that.sensitiveData)
                && Objects.equals(internalId, that.internalId)
                && Objects.equals(appointmentId, that.appointmentId)
                && Objects.equals(officerId, that.officerId)
                && Objects.equals(previousOfficerId, that.previousOfficerId)
                && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(updated, that.updated)
                && Objects.equals(updatedBy, that.updatedBy)
                && Objects.equals(created, that.created)
                && Objects.equals(deltaAt, that.deltaAt)
                && Objects.equals(companyName, that.companyName)
                && Objects.equals(companyStatus, that.companyStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, sensitiveData, internalId, appointmentId, officerId, previousOfficerId, companyNumber, updated, updatedBy, created, deltaAt, officerRoleSortOrder,
                companyName,
                companyStatus);
    }

    @Override
    public String toString() {
        return "CompanyAppointmentDocument{" +
                "id='" + id + '\'' +
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
                ", companyName='" + companyName + '\'' +
                ", companyStatus='" + companyStatus + '\'' +
                '}';
    }
}