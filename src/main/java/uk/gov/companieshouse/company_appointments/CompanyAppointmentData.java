package uk.gov.companieshouse.company_appointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.javafx.beans.IDProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "appointments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyAppointmentData {

	@Id
	private String id;

	private String appointmentId;
	private String companyName;
	private String companyNumber;
	private String companyStatus;

	private LocalDateTime created;
	private OfficerData data;
	private ServiceAddressData serviceAddressData;



//	appointed_on         => 'appointed_on',
//	links                => 'links',
//	occupation           => 'occupation',
//	officer_role         => 'officer_role',
//	resigned_on          => 'resigned_on',
//	identification       => 'identification',
//	former_names         => 'former_names',



}
