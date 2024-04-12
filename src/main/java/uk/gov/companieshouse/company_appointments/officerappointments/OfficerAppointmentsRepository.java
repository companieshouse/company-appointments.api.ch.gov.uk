package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

/**
 * Correct sorting: A list of, first, active officers sorted by appointed_on date in descending order (or
 * appointed_before if appointed_on is null), followed by resigned officers sorted by resigned_on date in descending
 * order.
 * <p>
 * If filter = true, this means the show-active-only filter on the frontend has been checked, and so we want to display
 * active appointments only. This results in the aggregation matching on records where data.resigned_on does not exist.
 * <p>
 * If filter = false, the aggregation matches on records where data.resigned_on either exists or does not - which
 * effectively just finds all records (i.e., both active and resigned appointments).
 */
@Repository
interface OfficerAppointmentsRepository extends MongoRepository<CompanyAppointmentDocument, String> {

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
        +           "'inactive': ["
        +               "{ $match: {"
        +                       "$and: ["
        +                           "{ 'data.resigned_on': { $exists: false } },"
        +                           "{ 'company_status': { $in: ['dissolved', 'closed', 'converted-closed'] } }"
        +                           "]"
        +                       "}"
        +                   "},"
        +                   "{ '$count': 'count'}"
        +               "],"
        +           "'total_results': [{ '$count': 'count' }]"
        +       "}"
        +   "}",
            "{ $unwind: {"
        +           "'path': '$total_results',"
        +           "'preserveNullAndEmptyArrays': true"
        +       "}"
        +   "}",
            "{ $unwind: {"
        +           "'path': '$inactive',"
        +           "'preserveNullAndEmptyArrays': true"
        +       "}"
        +   "}",
            "{ $project: {"
        +           "'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
        +           "'officer_appointments': { $slice: [{ $concatArrays: ['$active', '$resigned'] },  ?3, ?4] },"
        +           "'inactive_count': { '$ifNull': ['$inactive.count', NumberInt(0)] },"
        +           "'resigned_count': { $size: '$resigned' }"
        +       "}"
        +   "}"
    })
    @Meta(allowDiskUse = true)
    OfficerAppointmentsAggregate findOfficerAppointments(String officerId, boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

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

            "{ $project: { "
        +   "        'officer_id': 1, "
        +   "        'company_status': 1, "
        +   "        'data.resigned_on': 1, "
        +   "        'data.appointed_on': 1, "
        +   "        'data.appointed_before': 1 "
        +   "    }"
        +   "}",

            "{"
        +       "$addFields: {"
        +           "'__sort_active__': { $ifNull: ['$data.appointed_on', { $toDate: '$data.appointed_before' } ] }"
        +       "}"
        +   "}",

            "{ $facet: {"
        +           "'active': ["
        +               "{ $match: {'data.resigned_on': {$exists: false} } },"
        +               "{ $sort:  {'__sort_active__': -1 } },"
        +               "{ $project: { '_id': 1 } }"
        +               "],"
        +           "'resigned': ["
        +               "{ $match: {'data.resigned_on': {$exists: true} } },"
        +               "{ $sort:  {'data.resigned_on': -1} }"
        +               "{ $project: { '_id': 1 } }"
        +               "],"
        +           "'inactive': ["
        +               "{ $match: {"
        +                       "$and: ["
        +                           "{ 'data.resigned_on': { $exists: false } },"
        +                           "{ 'company_status': { $in: ['dissolved', 'closed', 'converted-closed'] } }"
        +                           "]"
        +                       "}"
        +                   "},"
        +                   "{ '$count': 'count'}"
        +               "],"
        +           "'total_results': [{ '$count': 'count' }]"
        +       "}"
        +   "}",

            "{ $unwind: {"
        +           "'path': '$total_results',"
        +           "'preserveNullAndEmptyArrays': true"
        +       "}"
        +   "}",

            "{ $unwind: {"
        +           "'path': '$inactive',"
        +           "'preserveNullAndEmptyArrays': true"
        +       "}"
        +   "}",

            "{ $project: {"
        +           "'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
        +           "'officer_appointments': { $slice: [{ $concatArrays: ['$active', '$resigned'] },  ?3, ?4] },"
        +           "'inactive_count': { '$ifNull': ['$inactive.count', NumberInt(0)] },"
        +           "'resigned_count': { $size: '$resigned' }"
        +       "}"
        +   "}"
    })
    @Meta(allowDiskUse = true)
    OfficerAppointmentsAggregate findOfficerAppointmentsSparseAggregate(String officerId, boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

    @Aggregation(pipeline = {
            "{ $match: { "
                    + "$and: [ "
                    + "{'officer_id': ?0 },"
                    + "{ $or: [ "
                    + "{ 'data.resigned_on': { $exists: false } },"
                    + "{ 'data.resigned_on': { $not: { $exists: ?1 } } }"
                    + "]"
                    + "},"
                    + "{ 'company_status': { $nin: ?2 } }"
                    + "]"
                    + "}"
                    + "}",

            "{ $project: { "
                    + "        'officer_role_sort_order': 1, "
                    + "        'company_status': 1, "
                    + "        'data.resigned_on': 1, "
                    + "    }"
                    + "}",

            "{ $facet: {"
                    + "'documents': ["
                    + "{ $sort:  {'officer_role_sort_order': 1 } },"
                    + "{ $project: { '_id': 1 } }"
                    + "],"
                    + "'resigned': ["
                    + "{ $match: {'data.resigned_on': {$exists: true} } },"
                    + "{ '$count': 'count'}"
                    + "],"
                    + "'inactive': ["
                    + "{ $match: {"
                    + "$and: ["
                    + "{ 'data.resigned_on': { $exists: false } },"
                    + "{ 'company_status': { $in: ['dissolved', 'closed', 'converted-closed'] } }"
                    + "]"
                    + "}"
                    + "},"
                    + "{ '$count': 'count'}"
                    + "],"
                    + "'total_results': [{ '$count': 'count' }]"
                    + "}"
                    + "}",

            "{ $unwind: {"
                    + "'path': '$total_results',"
                    + "'preserveNullAndEmptyArrays': true"
                    + "}"
                    + "}",

            "{ $unwind: {"
                    + "'path': '$resigned',"
                    + "'preserveNullAndEmptyArrays': true"
                    + "}"
                    + "}",

            "{ $unwind: {"
                    + "'path': '$inactive',"
                    + "'preserveNullAndEmptyArrays': true"
                    + "}"
                    + "}",

            "{ $project: {"
                    + "'total_results': { '$ifNull': ['$total_results.count', NumberInt(0)] },"
                    + "'officer_appointments': { $slice: [$documents,  ?3, ?4] },"
                    + "'inactive_count': { '$ifNull': ['$inactive.count', NumberInt(0)] },"
                    + "'resigned_count': { '$ifNull': ['$resigned.count', NumberInt(0)] }"
                    + "}"
                    + "}"
    })
    @Meta(allowDiskUse = true)
    OfficerAppointmentsAggregate findOfficerAppointmentsOfficerRoleSortOrder(String officerId, boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

    @Aggregation(pipeline = {
            "{ $match: { "
                    + "$and: [ "
                    + "{'officer_id': ?0 },"
                    + "{ $or: [ "
                    + "{ 'data.resigned_on': { $exists: false } },"
                    + "{ 'data.resigned_on': { $not: { $exists: ?1 } } }"
                    + "]"
                    + "},"
                    + "{ 'company_status': { $nin: ?2 } }"
                    + "]"
                    + "}"
                    + "}",
            "{ $sort:  {'officer_role_sort_order': 1 } }",
            "{ $skip: ?3 }",
            "{ $limit: ?4 }",
    })
    @Meta(allowDiskUse = true)
    List<CompanyAppointmentDocument> findOnlyOfficerAppointmentsOfficerRoleSortOrder(String officerId,
            boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

    @Query(value = "{ $and: [ "
            + "{ 'officer_id' : ?0 },"
            + "{'data.resigned_on': { $exists: true } } "
            + "] }", count = true)
    int countResigned(String officerId);

    @Query(value = "{ $and: [ "
            + "{ 'officer_id' : ?0 },"
            + "{ 'data.resigned_on': { $exists: false } },"
            + "{ 'company_status': { $in: ['dissolved', 'closed', 'converted-closed'] } }"
            + "] }", count = true)
    int countInactive(String officerId);

    @Query(value = "{ $and: [ "
            + "{'officer_id': ?0 },"
            + "{ $or: [ "
            + "{ 'data.resigned_on': { $exists: false } },"
            + "{ 'data.resigned_on': { $not: { $exists: ?1 } } }"
            + "] },"
            + "{ 'company_status': { $nin: ?2 } }"
            + "] }", count = true)
    int countTotal(String officerId, boolean filterEnabled, List<String> filterStatuses);

    @Aggregation(pipeline = {
            "{ $match: { _id: { $in: ?0 } } }",

            "{ $addFields: {"
                    + "'__sort_order__': { $indexOfArray: [?0, '$_id'] }"
                    + "}"
                    + "}",

            "{ $sort: { '__sort_order__': 1 } }"
                    + "}"
    })
    List<CompanyAppointmentDocument> findOfficerAppointmentsInIdList(Iterable<String> ids,
            boolean filterEnabled, List<String> filterStatuses);

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
            "{ $project: { "
                    +   "        'data.resigned_on': 1, "
                    +   "        'data.appointed_on': 1, "
                    +   "        'data.appointed_before': 1 "
                    +   "    }"
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
                    +               "{ $project: { '_id': 1 } }"
                    +               "],"
                    +           "'resigned': ["
                    +               "{ $match: {'data.resigned_on': {$exists: true} } },"
                    +               "{ $sort:  {'data.resigned_on': -1} }"
                    +               "{ $project: { '_id': 1 } }"
                    +               "]"
                    +       "}"
                    +   "}",
            "{ $project: {"
                    +           "'officer_appointments': { $slice: [{ $concatArrays: ['$active', '$resigned'] },  ?3, ?4] },"
                    +       "}"
                    +   "}"
    })
    @Meta(allowDiskUse = true)
    OfficerAppointmentsAggregate findOfficerAppointmentsNoCounts(String officerId, boolean filterEnabled,
            List<String> filterStatuses, int startIndex, int pageSize);

    Optional<CompanyAppointmentDocument> findFirstByOfficerId(String officerId);
}
