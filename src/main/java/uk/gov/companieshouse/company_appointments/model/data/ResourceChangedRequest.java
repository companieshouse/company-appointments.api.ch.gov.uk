package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;

public class ResourceChangedRequest {

    private final String contextId;
    private final String companyNumber;
    private final Object officersData;
    private final Boolean isDelete;

    public ResourceChangedRequest(String contextId, String companyNumber,
                                  Object officersData, Boolean isDelete) {
        this.contextId = contextId;
        this.companyNumber = companyNumber;
        this.officersData = officersData;
        this.isDelete = isDelete;
    }

    public String getContextId() {
        return contextId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public Object getOfficersData() {
        return officersData;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceChangedRequest that = (ResourceChangedRequest) o;
        return Objects.equals(contextId, that.contextId) &&
                Objects.equals(companyNumber, that.companyNumber) &&
                Objects.equals(officersData, that.officersData) &&
                Objects.equals(isDelete, that.isDelete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, companyNumber, officersData, isDelete);
    }
}
