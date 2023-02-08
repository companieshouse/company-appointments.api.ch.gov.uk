package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@Document(collection = "appointments")
public class CompanyAppointmentData {

    @Id
    private String id;

    private OfficerData data;

    @Field("company_status")
    private String companyStatus;

    @Field("officer_id")
    private String officerId;

    public CompanyAppointmentData(String id, OfficerData data, String companyStatus, String officerId) {
        this.id = id;
        this.data = data;
        this.companyStatus = companyStatus;
        this.officerId = officerId;
    }

    public CompanyAppointmentData(String id, OfficerData data, String companyStatus) {
        this.id = id;
        this.data = data;
        this.companyStatus = companyStatus;
    }

    public CompanyAppointmentData() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OfficerData getData() {
        return data;
    }

    public void setData(OfficerData data) {
        this.data = data;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

	public String getOfficerId() {
		return officerId;
	}

	public CompanyAppointmentData setOfficerId(String officerId) {
		this.officerId = officerId;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CompanyAppointmentData that = (CompanyAppointmentData) o;
		return Objects.equals(id, that.id) && Objects.equals(data, that.data) && Objects.equals(companyStatus, that.companyStatus) && Objects.equals(officerId,
				that.officerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, data, companyStatus, officerId);
	}
}
