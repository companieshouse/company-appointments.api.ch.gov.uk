package uk.gov.companieshouse.company_appointments;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDeltaData;

import java.util.Optional;

@Repository
public interface CompanyAppointmentDeltaRepository extends MongoRepository<CompanyAppointmentDeltaData, String> {

    void insert(CompanyAppointmentData companyAppointmentData);
}
