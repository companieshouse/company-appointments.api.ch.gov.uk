package uk.gov.companieshouse.company_appointments;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

import javax.swing.text.html.Option;

@Repository
public interface CompanyAppointmentRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<CompanyAppointmentData> readByCompanyNumberAndAppointmentID(String companyNumber, String appointmentId);

    @Query("{'company_number' : '?0'}")
    List<CompanyAppointmentData> readAllByCompanyNumber(String companyNumber);

    @Query("{'company_number' : '?0', 'data.resigned_on' : {$exists : false}}")
    List<CompanyAppointmentData> readAllByCompanyNumberForNotResigned(String companyNumber);


}
