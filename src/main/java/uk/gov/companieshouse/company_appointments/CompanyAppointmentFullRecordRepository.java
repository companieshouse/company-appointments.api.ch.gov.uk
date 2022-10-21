package uk.gov.companieshouse.company_appointments;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;

@Repository
public interface CompanyAppointmentFullRecordRepository extends MongoRepository<AppointmentApiEntity, String> {

    default AppointmentAPI insertOrUpdate(AppointmentAPI appointmentAPI) {
        return save(new AppointmentApiEntity(appointmentAPI));
    }

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<AppointmentApiEntity> readByCompanyNumberAndID(String companyNumber, String appointmentId);
}
