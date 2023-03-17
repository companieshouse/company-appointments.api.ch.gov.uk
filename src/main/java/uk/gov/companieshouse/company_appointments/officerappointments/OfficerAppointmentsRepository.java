package uk.gov.companieshouse.company_appointments.officerappointments;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

/**
 * Correct sorting: A list of, first, active officers sorted by appointed_on date in descending order (or appointed_before
 * if appointed_on is null), followed by resigned officers sorted by resigned_on date in descending order.
 */
// TODO: Include filtering in java docs
@Repository
public interface OfficerAppointmentsRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Aggregation(pipeline = {
            "{ '$match': { "
        +           "$and: [ "
        +                "{'officer_id': ?0 }, "
        +                "{ $or: [ "
        +                    "{ 'data.resigned_on': { $exists: false } }, "
        +                    "{ 'data.resigned_on': { $exists: ?1 } } "
        +                   "] "
        +                "}"
        +             "] "
        +         "}"
        +     "}",
            "{ '$facet': {"
        +           "'active': ["
        +               "{ $match: {'data.resigned_on': {$exists: false}} },"
        +               "{ $sort:  {'data.appointed_on': -1, 'data.appointed_before': -1 } }"
        +                   "],"
        +           "'resigned': ["
        +               "{ $match: {'data.resigned_on': {$exists: true} } },"
        +               "{ $sort:  {'data.resigned_on': -1} }"
        +                       "],"
        +           "'total_results': [{ '$count': 'count' }]"
        +               "}"
        +   "}",
            "{ '$unwind': {"
        +           "'path': '$total_results',"
        +           "'preserveNullAndEmptyArrays': true"
        +                 "}"
        +   "}",
            "{ '$project': {"
        +           "'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
        +           "'officer_appointments': {$concatArrays: ['$active', '$resigned']}"
        + "                }"
        +   "}"
    })
    OfficerAppointmentsAggregate findOfficerAppointments(String officerId, boolean noFilter);
}
