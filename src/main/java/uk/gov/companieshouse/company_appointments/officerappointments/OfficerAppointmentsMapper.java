package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.common.DateOfBirth;
import uk.gov.companieshouse.api.model.common.Links;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentsNameElements;
import uk.gov.companieshouse.api.model.officerappointments.OfficerAppointmentsApi;
import uk.gov.companieshouse.company_appointments.RoleHelper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Component
public class OfficerAppointmentsMapper {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";
    private static final String KIND = "personal-appointment";
    private static final String CORPORATE = "corporate";

    /**
     * Maps the the appointments returned from MongoDB to a list of officer appointments. Uses the
     * first active appointment, if present, otherwise uses the first appointment.
     *
     * @param aggregate The count and appointments list pairing returned by the repository.
     * @param request The request containing parameters for sorting, filtering and pagination.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    public Optional<OfficerAppointmentsApi> map(OfficerAppointmentsAggregate aggregate, OfficerAppointmentsRequest request) {
        return Optional.ofNullable(aggregate.getOfficerAppointments().stream()
                .filter(appointmentData -> appointmentData.getData().getResignedOn() == null)
                .findFirst()
                .orElse(aggregate.getOfficerAppointments().get(0))
        ).map(firstAppointment -> {
            OfficerAppointmentsApi officerAppointments = new OfficerAppointmentsApi();
            officerAppointments.setTotalResults(aggregate.getTotalResults().getCount());
            officerAppointments.setKind(KIND);

            Links links = new Links();
            links.setSelf(String.format("/officers/%s/appointments", request.getOfficerId()));
            officerAppointments.setLinks(links);

            // change when pagination is implemented
            officerAppointments.setStartIndex(0L);
            officerAppointments.setItemsPerPage(35L);

            officerAppointments.setCorporateOfficer(firstAppointment.getData().getOfficerRole().startsWith(CORPORATE));
            officerAppointments.setName(null); // TODO
            officerAppointments.setDateOfBirth(RoleHelper.isSecretary(firstAppointment) ? null : mapDateOfBirth(firstAppointment));
            officerAppointments.setEtag(firstAppointment.getData().getEtag());

            officerAppointments.setItems(aggregate.getOfficerAppointments().stream().map(a -> {
                AppointmentApi appointmentApi = new AppointmentApi();
                appointmentApi.setName(Optional.ofNullable(a.getData().getCompanyName())
                        .orElse(buildOfficerName(a)));
                appointmentApi.setNameElements(new AppointmentsNameElements());
                return appointmentApi;
            }).collect(Collectors.toList()));
            return officerAppointments;
        });
    }

    private DateOfBirth mapDateOfBirth(CompanyAppointmentData appointmentData) {
        return Optional.ofNullable(appointmentData.getData().getDateOfBirth())
                .map(dateOfBirth -> {
                    DateOfBirth dob = new DateOfBirth();
                    dob.setMonth((long) dateOfBirth.getMonthValue());
                    dob.setYear((long) dateOfBirth.getYear());
                    return dob;
                })
                .orElse(null);
    }

    private String buildOfficerName(CompanyAppointmentData companyAppointmentData) {
        String result = companyAppointmentData.getData().getSurname();
        if (companyAppointmentData.getData().getForename() != null || companyAppointmentData.getData().getOtherForenames() != null) {
            result = String.join(", ", companyAppointmentData.getData().getSurname(), Stream.of(companyAppointmentData.getData().getForename(), companyAppointmentData.getData().getOtherForenames())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")));
        }
        if (companyAppointmentData.getData().getTitle() != null && !companyAppointmentData.getData().getTitle().matches(TITLE_REGEX)) {
            result = String.join(", ", result, companyAppointmentData.getData().getTitle());
        }
        return result;
    }
}
