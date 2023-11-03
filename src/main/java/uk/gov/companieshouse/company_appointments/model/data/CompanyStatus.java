package uk.gov.companieshouse.company_appointments.model.data;

public enum CompanyStatus {
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

    private final String status;

    CompanyStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static CompanyStatus fromValue(String value) {
        for (CompanyStatus status : values()) {
            if (status.getStatus().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid company status '" + value + "'");
    }
}
