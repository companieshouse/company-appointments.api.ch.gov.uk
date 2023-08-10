package uk.gov.companieshouse.company_appointments.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static uk.gov.companieshouse.company_appointments.roles.DirectorRoles.CORPORATE_DIRECTOR;
import static uk.gov.companieshouse.company_appointments.roles.DirectorRoles.CORPORATE_NOMINEE_DIRECTOR;
import static uk.gov.companieshouse.company_appointments.roles.DirectorRoles.DIRECTOR;
import static uk.gov.companieshouse.company_appointments.roles.DirectorRoles.NOMINEE_DIRECTOR;
import static uk.gov.companieshouse.company_appointments.roles.LlpRoles.CORPORATE_LLP_DESIGNATED_MEMBER;
import static uk.gov.companieshouse.company_appointments.roles.LlpRoles.CORPORATE_LLP_MEMBER;
import static uk.gov.companieshouse.company_appointments.roles.LlpRoles.LLP_DESIGNATED_MEMBER;
import static uk.gov.companieshouse.company_appointments.roles.LlpRoles.LLP_MEMBER;
import static uk.gov.companieshouse.company_appointments.roles.SecretarialRoles.CORPORATE_NOMINEE_SECRETARY;
import static uk.gov.companieshouse.company_appointments.roles.SecretarialRoles.CORPORATE_SECRETARY;
import static uk.gov.companieshouse.company_appointments.roles.SecretarialRoles.NOMINEE_SECRETARY;
import static uk.gov.companieshouse.company_appointments.roles.SecretarialRoles.SECRETARY;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.mapper.SortMapper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Component
public class CompanyAppointmentRepositoryImpl implements CompanyAppointmentRepositoryExtension {

    private static final String DATA_OFFICER_ROLE = "data.officer_role";
    private static final String REMOVED = "removed";
    private static final String DISSOLVED = "dissolved";
    private static final String CONVERTED_CLOSED = "converted-closed";
    private final MongoTemplate mongoTemplate;
    private final SortMapper sortMapper;

    CompanyAppointmentRepositoryImpl(MongoTemplate mongoTemplate, SortMapper sortMapper) {
        this.mongoTemplate = mongoTemplate;
        this.sortMapper = sortMapper;
    }

    @Override
    public List<CompanyAppointmentDocument> getCompanyAppointments(String companyNumber,
                                                                   String orderBy, String registerType, int startIndex, int itemsPerPage,
                                                                   boolean registerView, boolean filterEnabled) {

        Criteria criteria = where("company_number").is(companyNumber);

        if (registerView) {
            criteria.and("data.resigned_on").exists(false);
            filterByRegisterType(criteria, registerType);
        } else if (filterEnabled) {
            criteria.and("data.resigned_on").exists(false)
                    .and("company_status").nin(List.of(DISSOLVED, REMOVED, CONVERTED_CLOSED));
        }

        Query query = query(criteria)
                .with(sortMapper.getSort(orderBy))
                .skip(startIndex)
                .limit(itemsPerPage);

        return mongoTemplate.find(query, CompanyAppointmentDocument.class);
    }

    private void filterByRegisterType(Criteria criteria, String registerType) {

        switch (registerType) {
            case "directors":
                criteria.and(DATA_OFFICER_ROLE)
                        .in(List.of(
                                DIRECTOR.getRole(),
                                CORPORATE_DIRECTOR.getRole(),
                                NOMINEE_DIRECTOR.getRole(),
                                CORPORATE_NOMINEE_DIRECTOR.getRole()));
                break;
            case "secretaries":
                criteria.and(DATA_OFFICER_ROLE)
                        .in(List.of(
                                SECRETARY.getRole(),
                                CORPORATE_SECRETARY.getRole(),
                                NOMINEE_SECRETARY.getRole(),
                                CORPORATE_NOMINEE_SECRETARY.getRole()));
                break;
            case "llp_members":
                criteria.and(DATA_OFFICER_ROLE)
                        .in(List.of(
                                LLP_MEMBER.getRole(),
                                CORPORATE_LLP_MEMBER.getRole(),
                                LLP_DESIGNATED_MEMBER.getRole(),
                                CORPORATE_LLP_DESIGNATED_MEMBER.getRole()));
                break;
            default:
                throw new IllegalArgumentException("Invalid registerType of " + registerType);
        }
    }
}