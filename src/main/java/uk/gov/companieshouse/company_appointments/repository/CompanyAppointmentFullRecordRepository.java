package uk.gov.companieshouse.company_appointments.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApiEntity;

@Repository
public interface CompanyAppointmentFullRecordRepository extends MongoRepository<DeltaAppointmentApiEntity, String> {

    default DeltaAppointmentApi insertOrUpdate(DeltaAppointmentApi api) {
        return save(new DeltaAppointmentApiEntity(api));
    }

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<DeltaAppointmentApiEntity> readByCompanyNumberAndID(String companyNumber, String appointmentId);

    @Query(value="{'company_number' : '?0', '_id' : '?1'}", delete = true)
    Optional<DeltaAppointmentApiEntity> deleteByCompanyNumberAndID(String companyNumber, String appointmentId);

    @Query("{ '_id': ?0 }")
    @Update("{ $set: { 'company_name': ?1, 'company_status': ?2, 'updated.at': ?3, 'etag': ?4 } }")
    long patchAppointmentNameStatus(String id, String companyName, String companyStatus, Instant at, String etag);
}
