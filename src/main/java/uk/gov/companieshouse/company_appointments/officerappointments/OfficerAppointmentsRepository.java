package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Repository
public interface OfficerAppointmentsRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Aggregation(pipeline = {
            "{ '$match': { 'officer_id': ?0 } }",
            "{ '$facet': { 'total_results': [{ '$count': 'count' }], "
                    + "'officer_appointments': [ { '$skip': 0 } ] }}",
            "{ '$unwind': { 'path': '$total_results', 'preserveNullAndEmptyArrays': true }}",
            "{ '$project': { 'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
                    + "'officer_appointments': '$officer_appointments' } }"
    })
    OfficerAppointmentsAggregate findOfficerAppointments(String officerId);
}
