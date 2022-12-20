package uk.gov.companieshouse.company_appointments;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
class SortMapper {

    private static final String APPOINTED_ON = "appointed_on";
    private static final String SURNAME = "surname";
    private static final String RESIGNED_ON = "resigned_on";

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "officer_role_sort_order")
            .and(Sort.by(Sort.Direction.ASC, "data.company_name", "data.surname"))
            .and(Sort.by(Sort.Direction.ASC, "data.forename"))
            .and(Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before"));
    private static final Sort APPOINTED_ON_SORT = Sort.by(Sort.Direction.DESC, "data.appointed_on", "data.appointed_before");
    private static final Sort SURNAME_SORT =  Sort.by(Sort.Direction.ASC, "data.company_name", "data.surname");
    private static final Sort RESIGNED_ON_SORT =  Sort.by(Sort.Direction.DESC, "data.resigned_on");

    public Sort getSort(String orderBy) throws BadRequestException {
        if (orderBy == null) {
            return DEFAULT_SORT;
        } else if (APPOINTED_ON.equals(orderBy)) {
            return APPOINTED_ON_SORT;
        } else if (SURNAME.equals(orderBy)) {
            return SURNAME_SORT;
        } else if (RESIGNED_ON.equals(orderBy)) {
            return RESIGNED_ON_SORT;
        } else {
            throw new BadRequestException(String.format("Invalid order by parameter [%s]", orderBy));
        }
    }
}
