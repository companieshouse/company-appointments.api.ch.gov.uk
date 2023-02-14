package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.DateOfBirth;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.RoleHelper;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

@Component
public class OfficerAppointmentsMapper {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";
    private static final String CORPORATE = "corporate";

    /**
     * Maps the the appointments returned from MongoDB to a list of officer appointments. Uses the
     * first active appointment, if present, otherwise uses the first appointment.
     *
     * @param aggregate The count and appointments list pairing returned by the repository.
     * @param request   The request containing parameters for sorting, filtering and pagination.
     * @return The optional OfficerAppointmentsApi for the response body.
     */
    public Optional<AppointmentList> mapOfficerAppointments(OfficerAppointmentsAggregate aggregate, OfficerAppointmentsRequest request) {
        return ofNullable(aggregate.getOfficerAppointments().stream()
                .filter(appointmentData -> appointmentData.getData().getResignedOn() == null)
                .findFirst()
                .orElse(aggregate.getOfficerAppointments().get(0))
        ).map(firstAppointment -> new AppointmentList()
                .dateOfBirth(RoleHelper.isSecretary(firstAppointment) ? null : mapDateOfBirth(firstAppointment))
                .etag(firstAppointment.getData().getEtag())
                .isCorporateOfficer(firstAppointment.getData().getOfficerRole().startsWith(CORPORATE))
                .itemsPerPage(35)
                .kind(AppointmentList.KindEnum.PERSONAL_APPOINTMENT)
                .links(new OfficerLinkTypes().self(String.format("/officers/%s/appointments", request.getOfficerId())))
                .items(aggregate.getOfficerAppointments().stream()
                        .map(appointment -> {
                            OfficerData data = appointment.getData();
                            return new OfficerAppointmentSummary()
                                    .address(ofNullable(data.getServiceAddress())
                                            .map(address -> new Address()
                                                    .addressLine1(address.getAddressLine1())
                                                    .addressLine2(address.getAddressLine2())
                                                    .careOf(address.getCareOf())
                                                    .country(address.getCountry())
                                                    .locality(address.getLocality())
                                                    .poBox(address.getPoBox())
                                                    .postalCode(address.getPostcode())
                                                    .premises(address.getPremises())
                                                    .region(address.getRegion()))
                                            .orElse(null))
                                    .appointedBefore(LocalDate.parse(data.getAppointedBefore(),
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK)))
                                    .appointedOn(data.getAppointedOn().toLocalDate())
                                    .appointedTo(new AppointedTo()
                                            .companyName(appointment.getCompanyName())
                                            .companyNumber(data.getCompanyNumber())
                                            .companyStatus(appointment.getCompanyStatus()))
                                    .contactDetails(ofNullable(data.getContactDetails())
                                            .map(contactDetails -> new ContactDetails()
                                                    .contactName(contactDetails.getContactName()))
                                            .orElse(null))
                                    .name(ofNullable(data.getCompanyName())
                                            .orElse(buildOfficerName(appointment)))
                                    .countryOfResidence(data.getCountryOfResidence())
                                    .formerNames(ofNullable(data.getFormerNameData())
                                            .map(formerNamesData -> formerNamesData.stream()
                                                    .map(names -> new FormerNames()
                                                            .forenames(names.getForenames())
                                                            .surname(names.getSurname()))
                                                    .collect(Collectors.toList()))
                                            .orElse(null))
                                    .identification(ofNullable(data.getIdentificationData())
                                            .map(identification -> new CorporateIdent()
                                                    .identificationType(CorporateIdent.IdentificationTypeEnum.fromValue(identification.getIdentificationType()))
                                                    .legalAuthority(identification.getLegalAuthority())
                                                    .legalForm(identification.getLegalForm())
                                                    .placeRegistered(identification.getPlaceRegistered())
                                                    .registrationNumber(identification.getRegistrationNumber()))
                                            .orElse(null))
                                    .isPre1992Appointment(data.getIsPre1992Appointment())
                                    .links(new AppointmentLinkTypes().company(String.format("/company/%s", data.getCompanyNumber())))
                                    .nameElements(new NameElements()
                                            .forename(data.getForename())
                                            .title(data.getTitle())
                                            .otherForenames(data.getOtherForenames())
                                            .surname(data.getSurname())
                                            .honours(data.getHonours()))
                                    .nationality(data.getNationality())
                                    .occupation(data.getOccupation())
                                    .officerRole(OfficerAppointmentSummary.OfficerRoleEnum.fromValue(data.getOfficerRole()))
                                    .principalOfficeAddress(ofNullable(data.getPrincipalOfficeAddress())
                                            .map(address -> new Address()
                                                    .addressLine1(address.getAddressLine1())
                                                    .addressLine2(address.getAddressLine2())
                                                    .careOf(address.getCareOf())
                                                    .country(address.getCountry())
                                                    .locality(address.getLocality())
                                                    .poBox(address.getPoBox())
                                                    .postalCode(address.getPostcode())
                                                    .premises(address.getPremises())
                                                    .region(address.getRegion()))
                                            .orElse(null))
                                    .resignedOn(ofNullable(data.getResignedOn())
                                            .map(LocalDateTime::toLocalDate)
                                            .orElse(null))
                                    .responsibilities(data.getResponsibilities());
                        }).collect(Collectors.toList()))
                .name(ofNullable(firstAppointment.getData().getCompanyName())
                        .orElse(buildOfficerName(firstAppointment)))
                .startIndex(0)
                .totalResults(aggregate.getTotalResults().getCount().intValue())
        );
    }

    private DateOfBirth mapDateOfBirth(CompanyAppointmentData appointmentData) {
        return ofNullable(appointmentData.getData().getDateOfBirth())
                .map(dateOfBirth -> new DateOfBirth()
                        .month(dateOfBirth.getMonthValue())
                        .year(dateOfBirth.getYear()))
                .orElse(null);
    }

    private String buildOfficerName(CompanyAppointmentData companyAppointmentData) {
        String result = Stream.of(companyAppointmentData.getData().getForename(), companyAppointmentData.getData().getOtherForenames(), companyAppointmentData.getData().getSurname())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        if (companyAppointmentData.getData().getTitle() != null && !companyAppointmentData.getData().getTitle().matches(TITLE_REGEX)) {
            result = companyAppointmentData.getData().getTitle() + result;
        }
        return result;
    }
}
