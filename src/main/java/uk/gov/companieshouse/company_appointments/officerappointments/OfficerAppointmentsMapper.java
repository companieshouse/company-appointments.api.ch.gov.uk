package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.api.officer.AppointmentList.KindEnum;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerAppointmentsItemsMapper.mapItems;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerDataMapper.mapDateOfBirth;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerDataMapper.mapIsCorporateOfficer;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerDataMapper.mapName;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;

@Component
public class OfficerAppointmentsMapper {

    private static final int ITEMS_PER_PAGE = 35;
    private static final int START_INDEX = 0;

    /**
     * Maps the the appointments returned from MongoDB to a list of officer appointments. Uses the
     * first active appointment, if present, otherwise uses the first appointment.
     *
     * @param aggregate The count and appointments list pairing returned by the repository.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    public Optional<AppointmentList> mapOfficerAppointments(OfficerAppointmentsAggregate aggregate) {
        return ofNullable(aggregate.getOfficerAppointments().stream()
                .filter(appointmentData -> appointmentData.getData().getResignedOn() == null)
                .findFirst()
                .orElse(aggregate.getOfficerAppointments().get(0)))
                .flatMap(firstAppointment -> ofNullable(firstAppointment.getData())
                        .map(data -> new AppointmentList()
                                .dateOfBirth(mapDateOfBirth(data))
                                .etag(data.getEtag())
                                .isCorporateOfficer(mapIsCorporateOfficer(data))
                                .itemsPerPage(ITEMS_PER_PAGE)
                                .kind(KindEnum.PERSONAL_APPOINTMENT)
                                .links(new OfficerLinkTypes().self(
                                        String.format("/officers/%s/appointments", firstAppointment.getOfficerId())))
                                .items(mapItems(aggregate.getOfficerAppointments()))
                                .name(mapName(data))
                                .startIndex(START_INDEX)
                                .totalResults(aggregate.getTotalResults().getCount().intValue())
                        ));
    }
}
