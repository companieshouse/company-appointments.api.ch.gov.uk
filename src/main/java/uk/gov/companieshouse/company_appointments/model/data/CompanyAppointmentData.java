package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "appointments")
public class CompanyAppointmentData {

	@Id
	private String id;

	private OfficerData data;

	public CompanyAppointmentData(String id, OfficerData data) {
		this.id = id;
		this.data = data;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CompanyAppointmentData)) return false;
		CompanyAppointmentData that = (CompanyAppointmentData) o;
		return Objects.equals(getId(), that.getId()) &&
				Objects.equals(getData(), that.getData());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getData());
	}
}
