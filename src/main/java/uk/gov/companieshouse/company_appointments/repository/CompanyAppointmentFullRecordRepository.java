package uk.gov.companieshouse.company_appointments.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
    void deleteByCompanyNumberAndID(String companyNumber, String appointmentId);
}
