package uk.gov.companieshouse.company_appointments;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;

@Repository
public interface AppointmentApiRepository extends MongoRepository<AppointmentApiEntity, String> {

    default AppointmentAPI insertOrUpdate(AppointmentAPI appointmentAPI) {
        return save(new AppointmentApiEntity(appointmentAPI));
    }

}