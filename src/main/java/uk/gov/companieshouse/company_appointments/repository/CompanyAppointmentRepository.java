package uk.gov.companieshouse.company_appointments.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Repository
public interface CompanyAppointmentRepository extends
        MongoRepository<CompanyAppointmentDocument, String>, CompanyAppointmentRepositoryExtension {

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<CompanyAppointmentDocument> readByCompanyNumberAndAppointmentID(String companyNumber,
            String appointmentId);

    default void insertOrUpdate(CompanyAppointmentDocument document) {
        save(document);
    }

    @Query("{'company_number' : '?0', '_id' : '?1'}")
    Optional<CompanyAppointmentDocument> readByCompanyNumberAndID(String companyNumber,
            String appointmentId);

    @Query(value = "{'company_number' : '?0', '_id' : '?1'}", delete = true)
    void deleteByCompanyNumberAndID(String companyNumber, String appointmentId);

    @Query("{ 'company_number': ?0 }")
    @Update("{ $set: { 'company_name': ?1, 'company_status': ?2, 'updated.at': ?3, 'data.etag': ?4 } }")
    long patchAppointmentNameStatusInCompany(String companyId, String companyName,
            String companyStatus, Instant at, String etag);
}
