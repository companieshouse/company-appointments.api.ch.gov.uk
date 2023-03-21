package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.api.officer.AppointmentList.KindEnum;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;

@Component
public class OfficerAppointmentsMapper {

    private final ItemsMapper itemsMapper;
    private final NameMapper nameMapper;
    private final DateOfBirthMapper dobMapper;
    private final OfficerRoleMapper roleMapper;

    public OfficerAppointmentsMapper(ItemsMapper itemsMapper,
            NameMapper nameMapper,
            DateOfBirthMapper dobMapper,
            OfficerRoleMapper roleMapper) {
        this.itemsMapper = itemsMapper;
        this.nameMapper = nameMapper;
        this.dobMapper = dobMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * Maps the appointments returned from MongoDB to a list of officer appointments
     * alongside top level fields, relating to the first appointment found.
     *
     * @param aggregate The count and appointments list pairing returned by the repository.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    protected Optional<AppointmentList> mapOfficerAppointments(Integer startIndex, Integer itemsPerPage, OfficerAppointmentsAggregate aggregate) {
        return aggregate.getOfficerAppointments().stream()
                .findFirst()
                .flatMap(firstAppointment -> ofNullable(firstAppointment.getData())
                        .map(data -> new AppointmentList()
                                .dateOfBirth(dobMapper.map(data.getDateOfBirth(), data.getOfficerRole()))
                                .etag(data.getEtag())
                                .isCorporateOfficer(roleMapper.mapIsCorporateOfficer(data.getOfficerRole()))
                                .itemsPerPage(itemsPerPage)
                                .kind(KindEnum.PERSONAL_APPOINTMENT)
                                .links(new OfficerLinkTypes().self(
                                        String.format("/officers/%s/appointments", firstAppointment.getOfficerId())))
                                .items(itemsMapper.map(aggregate.getOfficerAppointments()))
                                .name(nameMapper.map(data))
                                .startIndex(startIndex)
                                .totalResults(aggregate.getTotalResults())
                        ));
    }
}
