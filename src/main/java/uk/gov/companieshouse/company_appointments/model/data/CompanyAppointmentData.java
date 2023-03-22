package uk.gov.companieshouse.company_appointments.model.data;

import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "appointments")
public class CompanyAppointmentData {

    @Id
    private String id;

    private OfficerData data;

    @Field("company_status")
    private String companyStatus;

    @Field("officer_id")
    private String officerId;

    @Field("company_name")
    private String companyName;

    public CompanyAppointmentData(String id, OfficerData data, String companyStatus, String officerId, String companyName) {
        this.id = id;
        this.data = data;
        this.companyStatus = companyStatus;
        this.officerId = officerId;
        this.companyName = companyName;
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

	public void setOfficerId(String officerId) {
		this.officerId = officerId;
	}

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompanyAppointmentData data1 = (CompanyAppointmentData) o;
        return Objects.equals(id, data1.id) && Objects.equals(data, data1.data) && Objects.equals(companyStatus, data1.companyStatus) && Objects.equals(officerId,
                data1.officerId) && Objects.equals(companyName, data1.companyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, companyStatus, officerId, companyName);
    }
}
