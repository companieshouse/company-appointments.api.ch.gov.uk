package uk.gov.companieshouse.company_appointments.model;

import java.util.Objects;

public class FetchAppointmentsRequest {

    private final String companyNumber;
    private final String filter;
    private final String orderBy;
    private final Integer startIndex;
    private final Integer itemsPerPage;
    private final Boolean registerView;
    private final String registerType;

    private FetchAppointmentsRequest(Builder builder) {
        companyNumber = builder.companyNumber;
        filter = builder.filter;
        orderBy = builder.orderBy;
        startIndex = builder.startIndex;
        itemsPerPage = builder.itemsPerPage;
        registerView = builder.registerView;
        registerType = builder.registerType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getFilter() {
        return filter;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public Boolean getRegisterView() {
        return registerView;
    }

    public String getRegisterType() {
        return registerType;
    }

    public static final class Builder {
        private String companyNumber;
        private String filter;
        private String orderBy;
        private Integer startIndex;
        private Integer itemsPerPage;
        private Boolean registerView;
        private String registerType;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withFilter(String filter) {
            this.filter = filter;
            return this;
        }

        public Builder withOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Builder withStartIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder withItemsPerPage(Integer itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public Builder withRegisterView(Boolean registerView) {
            this.registerView = registerView;
            return this;
        }

        public Builder withRegisterType(String registerType) {
            this.registerType = registerType;
            return this;
        }

        public FetchAppointmentsRequest build() {
            return new FetchAppointmentsRequest(this);
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
        FetchAppointmentsRequest request = (FetchAppointmentsRequest) o;
        return Objects.equals(companyNumber, request.companyNumber) &&
                Objects.equals(filter, request.filter) &&
                Objects.equals(orderBy, request.orderBy) &&
                Objects.equals(startIndex, request.startIndex) &&
                Objects.equals(itemsPerPage, request.itemsPerPage) &&
                Objects.equals(registerView, request.registerView) &&
                Objects.equals(registerType, request.registerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyNumber, filter, orderBy, startIndex, itemsPerPage, registerView, registerType);
    }
}
