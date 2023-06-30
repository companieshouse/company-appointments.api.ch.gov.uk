package uk.gov.companieshouse.company_appointments.service;

public class FetchAppointmentsRequest {

    private String companyNumber;
    private String filter;
    private String orderBy;
    private Integer startIndex;
    private Integer itemsPerPage;
    private Boolean registerView;
    private String registerType;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public FetchAppointmentsRequest companyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public FetchAppointmentsRequest filter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public FetchAppointmentsRequest orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public FetchAppointmentsRequest startIndex(Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public FetchAppointmentsRequest itemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    public Boolean getRegisterView() {
        return registerView;
    }

    public FetchAppointmentsRequest registerView(Boolean registerView) {
        this.registerView = registerView;
        return this;
    }

    public String getRegisterType() {
        return registerType;
    }

    public FetchAppointmentsRequest registerType(String registerType) {
        this.registerType = registerType;
        return this;
    }
}
