package uk.gov.companieshouse.company_appointments;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.company_appointments.model.data.AppointmentApiEntity;

import java.util.Optional;

@Repository
public interface CompanyAppointmentFullRecordRepository extends MongoRepository<AppointmentApiEntity, String> {

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<AppointmentApiEntity> findByCompanyNumberAndAppointmentID(String companyNumber, String appointmentId);

    default AppointmentAPI insertOrUpdate(AppointmentAPI appointmentAPI) {
        return save(new AppointmentApiEntity(appointmentAPI));
    }

    /**
     * Checks for the existence of an appointment with a particular ID and a deltaAt greater then
     * the one provided. The comparison of deltaAt is lexicographical as the field is a string.
     * When the field is a numeric timestamp this produces the desired behaviour, however, if the
     * deltaAt field contains characters which are not numeric then is will produce incorrect results.
     * As the incoming data is assumed to be correct, this shouldn't be an issue.
     *
     * @param id The id of the appointment to check
     * @param deltaAt The incoming deltaAt to compare to
     * @return if there exists an appointment with a newer deltaAt field
     */
    boolean existsByIdAndDeltaAtGreaterThanEqual(final String id, final String deltaAt);

    /**
     * Checks for the existence of an appointment with a particular ID and a companyNumber.
     *
     * @param id The id of the appointment to check
     * @param companyNumber The incoming companyNumber to compare to
     * @return if there exists an appointment with the companyNumber
     */
    boolean existsByIdAndCompanyNumber(final String id, final String companyNumber);
}
