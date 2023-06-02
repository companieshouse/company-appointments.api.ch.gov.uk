package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

/**
 * Correct sorting: A list of, first, active officers sorted by appointed_on date in descending order (or appointed_before
 * if appointed_on is null), followed by resigned officers sorted by resigned_on date in descending order.
 *
 * If filter = true, this means the show-active-only filter on the frontend has been checked, and so we want to
 * display active appointments only. This results in the aggregation matching on records where
 * data.resigned_on does not exist.
 *
 * If filter = false, the aggregation matches on records where data.resigned_on either exists or does not - which effectively
 * just finds all records (i.e., both active and resigned appointments).
 */
@Repository
public interface OfficerAppointmentsRepository extends MongoRepository<CompanyAppointmentData, String> {

    @Aggregation(pipeline = {
            "{ $match: { "
        +           "$and: [ "
        +               "{'officer_id': ?0 },"
        +               "{ $or: [ "
        +                   "{ 'data.resigned_on': { $exists: false } },"
        +                   "{ 'data.resigned_on': { $not: { $exists: ?1 } } }"
        +                   "]"
        +               "},"
        +               "{ 'company_status': { $nin: ?2 } }"
        +           "]"
        +       "}"
        +   "}",
            "{"
        +       "$addFields: {"
        +           "'__sort_active__': { $ifNull: ['$data.appointed_on', { $toDate: '$data.appointed_before' } ] }"
        +       "}"
        +   "}",
            "{ $facet: {"
        +           "'active': ["
        +               "{ $match: {'data.resigned_on': {$exists: false} } },"
        +               "{ $sort:  {'__sort_active__': -1 } }"
        +               "],"
        +           "'resigned': ["
        +               "{ $match: {'data.resigned_on': {$exists: true} } },"
        +               "{ $sort:  {'data.resigned_on': -1} }"
        +               "],"
        +           "'total_results': [{ '$count': 'count' }]"
        +       "}"
        +   "}",
            "{ $unwind: {"
        +           "'path': '$total_results',"
        +           "'preserveNullAndEmptyArrays': true"
        +       "}"
        +   "}",
            "{ $project: {"
        +           "'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
        +           "'officer_appointments': { $slice: [{ $concatArrays: ['$active', '$resigned'] },  ?3, ?4] }"
        +       "}"
        +   "}"
    })
    OfficerAppointmentsAggregate findOfficerAppointments(String officerId, boolean filterEnabled, List<String> statusFilter, int startIndex, int pageSize);

    Optional<CompanyAppointmentData> findFirstByOfficerId(String officerId);

    @Aggregation(pipeline = {
            "{ $match: { 'officer_id': '?0'} }",
            "{ $facet: { "
+               "'active': [ "
+                   "{ $match: { $and: [ "
+                               "{ 'data.resigned_on': { $exists: false }},"
+                               "{ 'company_status': 'active'}"
+                           "]"
+                       "}"
+                   "}"
+               "],"
+               "'inactive': [ "
+                   "{ $match: { $and: [ "
+                               "{ 'data.resigned_on': { $exists: false }},"
+                               "{ $or: [ "
+                                   "{'company_status': 'dissolved'},"
+                                   "{'company_status': 'removed'},"
+                                   "{'company_status': 'converted-closed'}"
+                                   "]"
+                               "}"
+                           "]"
+                       "}"
+                   "}"
+               "],"
+               "'resigned': [ "
+                   "{ $match: { $and: [ "
+                               "{ 'data.resigned_on': { $exists: true }}"
+                           "]"
+                       "}"
+                   "}"
+               "],"
+               "'total': [{ '$count': 'count' }]"
+               "}"
+           "}",
            "{ "
+               "$unwind: { "
+                   "'path': '$total',"
+                   "'preserveNullAndEmptyArrays': true"
+               "}"
+           "}",
            "{"
+               "$project: { "
+                   "'active_count': { $size: '$active' },"
+                   "'inactive_count': { $size: '$inactive'},"
+                   "'resigned_count': { $size: '$resigned'},"
+                   "'total_results': { $ifNull: ['$total.count', NumberInt(0)] }"
+               "}"
+           "}"
    })
    AppointmentCounts findOfficerAppointmentCounts(String officerId);
}
