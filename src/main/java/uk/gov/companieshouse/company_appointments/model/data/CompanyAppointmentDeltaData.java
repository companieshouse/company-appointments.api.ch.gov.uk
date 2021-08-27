package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "delta_appointments")
public class CompanyAppointmentDeltaData extends CompanyAppointmentData{


	public CompanyAppointmentDeltaData(String id, OfficerData data) {
		super(id, data);
	}
}
