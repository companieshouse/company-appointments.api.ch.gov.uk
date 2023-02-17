package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.api.officer.AppointmentList.KindEnum;
import static uk.gov.companieshouse.company_appointments.officerappointments.DateOfBirthMapper.mapDateOfBirth;
import static uk.gov.companieshouse.company_appointments.officerappointments.ItemsMapper.mapItems;
import static uk.gov.companieshouse.company_appointments.officerappointments.NameMapper.mapName;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerRoleMapper.mapIsCorporateOfficer;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;

@Component
public class OfficerAppointmentsMapper {

    private static final int ITEMS_PER_PAGE = 35;
    private static final int START_INDEX = 0;

    /**
     * Maps the appointments returned from MongoDB to a list of officer appointments
     * alongside top level fields, relating to the first appointment found.
     *
     * @param aggregate The count and appointments list pairing returned by the repository.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    protected Optional<AppointmentList> mapOfficerAppointments(OfficerAppointmentsAggregate aggregate) {
        return aggregate.getOfficerAppointments().stream()
                .findFirst()
                .flatMap(firstAppointment -> ofNullable(firstAppointment.getData())
                        .map(data -> new AppointmentList()
                                .dateOfBirth(mapDateOfBirth(data.getDateOfBirth(), data.getOfficerRole()))
                                .etag(data.getEtag())
                                .isCorporateOfficer(mapIsCorporateOfficer(data.getOfficerRole()))
                                .itemsPerPage(ITEMS_PER_PAGE)
                                .kind(KindEnum.PERSONAL_APPOINTMENT)
                                .links(new OfficerLinkTypes().self(
                                        String.format("/officers/%s/appointments", firstAppointment.getOfficerId())))
                                .items(mapItems(aggregate.getOfficerAppointments()))
                                .name(mapName(data))
                                .startIndex(START_INDEX)
                                .totalResults(aggregate.getTotalResults())
                        ));
    }
}
