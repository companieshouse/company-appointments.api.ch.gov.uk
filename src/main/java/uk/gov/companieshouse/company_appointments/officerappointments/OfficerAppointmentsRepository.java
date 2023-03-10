package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Repository
public interface OfficerAppointmentsRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Aggregation(pipeline = {
            "{ '$match': { 'officer_id': ?0 } }",
            "{ $addFields: {"
                    + "'sort_active': { $ifNull: ['$data.appointed_on', '$data.appointed_before']}"
                    + "}"
        + "}",
            "{ '$facet': {"
                    + "'active': ["
                        + "{ $match: {'data.resigned_on': {$exists: false}} },"
                        + "{ $sort:  {'sort_active': -1} }"
                    + "],"
                    + "'resigned': ["
                        + "{ $match: {'data.resigned_on': {$exists: true}} },"
                        + "{ $sort:  {'data.resigned_on': -1} }"
                    + "],"
                    + "'total_results': [{ '$count': 'count' }]"
                    + "}"
        + "}",
            "{ '$unwind': {"
                    + "'path': '$total_results',"
                    + "'preserveNullAndEmptyArrays': true"
                    + "}"
        + "}",
            "{ '$project': {"
                    + "'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
                    + "'officer_appointments': {$concatArrays: ['$active', '$resigned']}"
                    + "}"
        + "}"
    })
    OfficerAppointmentsAggregate findOfficerAppointments(String officerId);
}
