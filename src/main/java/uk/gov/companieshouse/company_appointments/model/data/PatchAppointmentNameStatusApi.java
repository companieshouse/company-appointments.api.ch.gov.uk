package uk.gov.companieshouse.company_appointments.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PatchAppointmentNameStatusApi {

    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("company_status")
    private String companyStatus;

    public PatchAppointmentNameStatusApi() {
    }

    public PatchAppointmentNameStatusApi(String companyName, String companyStatus) {
        this.companyName = companyName;
        this.companyStatus = companyStatus;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public PatchAppointmentNameStatusApi companyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public PatchAppointmentNameStatusApi companyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
        return this;
    }
}
