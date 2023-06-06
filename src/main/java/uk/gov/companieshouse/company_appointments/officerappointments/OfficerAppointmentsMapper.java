package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.api.officer.AppointmentList.KindEnum;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

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
     * @param firstAppointment The first appointment found in the db.
     * @param aggregate        The count and appointments list pairing returned by the repository.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    protected Optional<AppointmentList> mapOfficerAppointments(Integer startIndex, Integer itemsPerPage, CompanyAppointmentData firstAppointment, OfficerAppointmentsAggregate aggregate, AppointmentCounts appointmentCounts) {
        Integer totalResults = aggregate.getTotalResults();
        Integer inactiveCount;
        Integer resignedCount;
        Integer activeCount;

        if (appointmentCounts != null) {
            inactiveCount = appointmentCounts.getInactiveCount();
            resignedCount = appointmentCounts.getResignedCount();
            activeCount = totalResults - inactiveCount - resignedCount;

        } else {
            resignedCount = null;
            inactiveCount = null;
            activeCount = null;
        }

        return ofNullable(firstAppointment.getData())
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
                                .activeCount(activeCount)
                                .inactiveCount(inactiveCount)
                                .resignedCount(resignedCount)
                                .totalResults(totalResults));
    }
}
