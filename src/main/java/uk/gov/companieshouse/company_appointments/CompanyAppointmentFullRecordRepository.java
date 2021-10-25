package uk.gov.companieshouse.company_appointments;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;

import java.util.Optional;

@Repository
public interface CompanyAppointmentFullRecordRepository extends MongoRepository<AppointmentApiEntity, String> {

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<AppointmentApiEntity> findByCompanyNumberAndAppointmentID(String companyNumber, String appointmentId);

}
