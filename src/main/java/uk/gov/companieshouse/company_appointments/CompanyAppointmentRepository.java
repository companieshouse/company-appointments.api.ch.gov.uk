package uk.gov.companieshouse.company_appointments;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Repository
public interface CompanyAppointmentRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<CompanyAppointmentData> readByCompanyNumberAndAppointmentID(String companyNumber, String appointmentId);

}
