package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerDataMapper.mapName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import uk.gov.companieshouse.api.officer.Address;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.ContactDetails;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.FormerNames;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerData;

public class OfficerAppointmentsItemsMapper {

    private OfficerAppointmentsItemsMapper() {
    }

    public static List<OfficerAppointmentSummary> mapItems(List<CompanyAppointmentData> appointments) {
        return appointments.stream()
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
                            .appointedBefore(ofNullable(data.getAppointedBefore())
                                    .map(appointedBefore -> LocalDate.parse(appointedBefore, DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK)))
                                    .orElse(null))
                            .appointedOn(ofNullable(data.getAppointedOn())
                                    .map(LocalDateTime::toLocalDate)
                                    .orElse(null))
                            .appointedTo(new AppointedTo()
                                    .companyName(appointment.getCompanyName())
                                    .companyNumber(data.getCompanyNumber())
                                    .companyStatus(appointment.getCompanyStatus()))
                            .contactDetails(ofNullable(data.getContactDetails())
                                    .map(contactDetails -> new ContactDetails()
                                            .contactName(contactDetails.getContactName()))
                                    .orElse(null))
                            .name(mapName(data))
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
                            .officerRole(ofNullable(data.getOfficerRole())
                                    .map(OfficerAppointmentSummary.OfficerRoleEnum::fromValue)
                                    .orElse(null))
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
                }).collect(Collectors.toList());
    }
}
