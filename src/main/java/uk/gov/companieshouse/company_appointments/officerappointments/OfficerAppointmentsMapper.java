package uk.gov.companieshouse.company_appointments.officerappointments;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.common.Address;
import uk.gov.companieshouse.api.model.common.DateOfBirth;
import uk.gov.companieshouse.api.model.common.Links;
import uk.gov.companieshouse.api.model.officerappointments.AppointedToApi;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentApi;
import uk.gov.companieshouse.api.model.officerappointments.AppointmentsNameElements;
import uk.gov.companieshouse.api.model.officerappointments.OfficerAppointmentsApi;
import uk.gov.companieshouse.api.model.officers.FormerNamesApi;
import uk.gov.companieshouse.company_appointments.RoleHelper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

import static java.util.Optional.*;

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
     * @param request   The request containing parameters for sorting, filtering and pagination.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    public Optional<OfficerAppointmentsApi> mapOfficerAppointments(OfficerAppointmentsAggregate aggregate, OfficerAppointmentsRequest request) {
        return ofNullable(aggregate.getOfficerAppointments().stream()
                .filter(appointmentData -> appointmentData.getData().getResignedOn() == null)
                .findFirst()
                .orElse(aggregate.getOfficerAppointments().get(0))
        ).map(firstAppointment -> {
            OfficerAppointmentsApi officerAppointments = new OfficerAppointmentsApi();
            officerAppointments.setDateOfBirth(RoleHelper.isSecretary(firstAppointment) ? null : mapDateOfBirth(firstAppointment));
            officerAppointments.setEtag(firstAppointment.getData().getEtag());
            officerAppointments.setCorporateOfficer(firstAppointment.getData().getOfficerRole().startsWith(CORPORATE));
            officerAppointments.setItemsPerPage(35L);
            officerAppointments.setKind(KIND);

            Links links = new Links();
            links.setSelf(String.format("/officers/%s/appointments", request.getOfficerId()));
            officerAppointments.setLinks(links);

            officerAppointments.setName(ofNullable(firstAppointment.getData().getCompanyName())
                    .orElse(buildOfficerName(firstAppointment)));
            officerAppointments.setStartIndex(0L);
            officerAppointments.setTotalResults(aggregate.getTotalResults().getCount());

            officerAppointments.setItems(aggregate.getOfficerAppointments().stream()
                    .map(appointment -> {
                        OfficerData data = appointment.getData();
                        AppointmentApi appointmentApi = new AppointmentApi();

                        appointmentApi.setAddress(ofNullable(data.getServiceAddress())
                                .map(serviceAddress -> {
                                    Address address = new Address();
                                    address.setAddressLine1(serviceAddress.getAddressLine1());
                                    address.setAddressLine2(serviceAddress.getAddressLine2());
                                    address.setCareOf(serviceAddress.getCareOf());
                                    address.setCountry(serviceAddress.getCountry());
                                    address.setLocality(serviceAddress.getLocality());
                                    address.setPoBox(serviceAddress.getPoBox());
                                    address.setPostalCode(serviceAddress.getPostcode());
                                    address.setPremises(serviceAddress.getPremises());
                                    address.setRegion(serviceAddress.getRegion());
                                    return address;
                                })
                                .orElse(null));

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK);
                        appointmentApi.setAppointedBefore(LocalDate.parse(data.getAppointedBefore(), formatter));

                        appointmentApi.setAppointedOn(data.getAppointedOn().toLocalDate());

                        AppointedToApi appointedToApi = new AppointedToApi();
                        appointedToApi.setCompanyName(data.getCompanyName());
                        appointedToApi.setCompanyNumber(data.getCompanyNumber());
                        appointedToApi.setCompanyStatus(appointment.getCompanyStatus());
                        appointmentApi.setAppointedTo(appointedToApi);

                        appointmentApi.setCountryOfResidence(data.getCountryOfResidence());

                        appointmentApi.setFormerNames(data.getFormerNameData().stream()
                                .map(names -> {
                                    FormerNamesApi formerNames = new FormerNamesApi();
                                    formerNames.setForenames(names.getForenames());
                                    formerNames.setSurname(names.getSurname());
                                    return formerNames;
                                })
                                .collect(Collectors.toList()));

                        appointmentApi.setName(ofNullable(data.getCompanyName())
                                .orElse(buildOfficerName(appointment)));
                        appointmentApi.setNameElements(new AppointmentsNameElements());
                        return appointmentApi;
                    }).collect(Collectors.toList()));
            return officerAppointments;
        });
    }

    private DateOfBirth mapDateOfBirth(CompanyAppointmentData appointmentData) {
        return ofNullable(appointmentData.getData().getDateOfBirth())
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
