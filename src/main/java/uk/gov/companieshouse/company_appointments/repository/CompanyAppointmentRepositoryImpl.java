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
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Component
public class CompanyAppointmentRepositoryImpl implements CompanyAppointmentRepositoryExtension {

    private static final String DATA_OFFICER_ROLE = "data.officer_role";
    private final MongoTemplate mongoTemplate;
    private final SortMapper sortMapper;

    CompanyAppointmentRepositoryImpl(MongoTemplate mongoTemplate, SortMapper sortMapper) {
        this.mongoTemplate = mongoTemplate;
        this.sortMapper = sortMapper;
    }

    @Override
    public List<CompanyAppointmentData> getCompanyAppointmentData(String companyNumber,
            String orderBy, String registerType, int startIndex, int itemsPerPage,
            boolean registerView, boolean filterActiveOnly) {

        Criteria criteria = where("company_number").is(companyNumber);

        if (filterActiveOnly) {
            criteria.and("data.resigned_on").exists(false);
        }

        if (registerView) {
            filterByRegisterType(criteria, registerType);
        }

        Query query = query(criteria)
                .with(sortMapper.getSort(orderBy))
                .skip(startIndex)
                .limit(itemsPerPage);

        return mongoTemplate.query(CompanyAppointmentData.class)
                .matching(query)
                .all();
    }

    private void filterByRegisterType(Criteria criteria, String registerType) {

        switch (registerType) {
            case "directors":
                criteria.orOperator(
                        where(DATA_OFFICER_ROLE).is(DIRECTOR.getRole()),
                        where(DATA_OFFICER_ROLE).is(CORPORATE_DIRECTOR.getRole()),
                        where(DATA_OFFICER_ROLE).is(NOMINEE_DIRECTOR.getRole()),
                        where(DATA_OFFICER_ROLE).is(CORPORATE_NOMINEE_DIRECTOR.getRole()));
                break;
            case "secretaries":
                criteria.orOperator(
                        where(DATA_OFFICER_ROLE).is(SECRETARY.getRole()),
                        where(DATA_OFFICER_ROLE).is(CORPORATE_SECRETARY.getRole()),
                        where(DATA_OFFICER_ROLE).is(NOMINEE_SECRETARY.getRole()),
                        where(DATA_OFFICER_ROLE).is(CORPORATE_NOMINEE_SECRETARY.getRole()));
                break;
            case "llp_members":
                criteria.orOperator(
                        where(DATA_OFFICER_ROLE).is(LLP_MEMBER.getRole()),
                        where(DATA_OFFICER_ROLE).is(CORPORATE_LLP_MEMBER.getRole()),
                        where(DATA_OFFICER_ROLE).is(LLP_DESIGNATED_MEMBER.getRole()),
                        where(DATA_OFFICER_ROLE).is(CORPORATE_LLP_DESIGNATED_MEMBER.getRole()));
                break;
            default:
                throw new IllegalArgumentException("Invalid registerType of " + registerType);
        }
    }
}