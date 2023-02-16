package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.company_appointments.officerappointments.AddressMapper.mapAddress;
import static uk.gov.companieshouse.company_appointments.officerappointments.ContactDetailsMapper.mapContactDetails;
import static uk.gov.companieshouse.company_appointments.officerappointments.FormerNamesMapper.mapFormerNames;
import static uk.gov.companieshouse.company_appointments.officerappointments.LocalDateMapper.mapLocalDate;
import static uk.gov.companieshouse.company_appointments.officerappointments.OfficerDataMapper.mapName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.CorporateIdent;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

public class OfficerAppointmentsItemsMapper {

    private OfficerAppointmentsItemsMapper() {
    }

    public static List<OfficerAppointmentSummary> mapItems(List<CompanyAppointmentData> appointments) {
        return appointments.stream()
                .map(appointment -> ofNullable(appointment.getData())
                        .map(data -> new OfficerAppointmentSummary()
                                .address(mapAddress(data.getServiceAddress()))
                                .appointedBefore(mapLocalDate(data.getAppointedBefore()))
                                .appointedOn(mapLocalDate(data.getAppointedOn()))
                                .appointedTo(new AppointedTo()
                                        .companyName(appointment.getCompanyName())
                                        .companyNumber(data.getCompanyNumber())
                                        .companyStatus(appointment.getCompanyStatus()))
                                .contactDetails(mapContactDetails(data.getContactDetails()))
                                .name(mapName(data))
                                .countryOfResidence(data.getCountryOfResidence())
                                .formerNames(mapFormerNames(data.getFormerNameData()))
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
                                .principalOfficeAddress(mapAddress(data.getPrincipalOfficeAddress()))
                                .resignedOn(ofNullable(data.getResignedOn())
                                        .map(LocalDateTime::toLocalDate)
                                        .orElse(null))
                                .responsibilities(data.getResponsibilities()))
                        .orElse(null))
                .collect(Collectors.toList());
    }
}
