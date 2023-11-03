package uk.gov.companieshouse.company_appointments.model.data;

public enum AcceptedCompanyStatuses {
    ACTIVE("active"),
    DISSOLVED("dissolved"),
    LIQUIDATION("liquidation"),
    RECEIVERSHIP("receivership"),
    CONVERTED_CLOSED("converted-closed"),
    OPEN("open"),
    CLOSED("closed"),
    INSOLVENCY_PROCEEDINGS("insolvency-proceedings"),
    VOLUNTARY_ARRANGEMENT("voluntary-arrangement"),
    ADMINISTRATION("administration"),
    REGISTERED("registered"),
    REMOVED("removed");

    private final String validCompanyStatus;

    AcceptedCompanyStatuses(String validCompanyStatus) {
        this.validCompanyStatus = validCompanyStatus;
    }

    public String getValidCompanyStatus() {
        return validCompanyStatus;
    }
}
