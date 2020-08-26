package uk.gov.companieshouse.company_appointments;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CompanyAppointmentRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    public CompanyAppointmentData readByCompanyNumberAndAppointmentID(String companyNumber, String appointmentId);

}
