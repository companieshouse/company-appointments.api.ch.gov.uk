package uk.gov.companieshouse.company_appointments;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDeltaData;

@Repository
public interface CompanyAppointmentDeltaRepository extends MongoRepository<CompanyAppointmentDeltaData, String> {

    void insert(AppointmentAPI companyAppointmentData);
}