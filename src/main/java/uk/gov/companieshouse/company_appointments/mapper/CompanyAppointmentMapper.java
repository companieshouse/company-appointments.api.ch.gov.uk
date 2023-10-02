package uk.gov.companieshouse.company_appointments.mapper;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.Address;
import uk.gov.companieshouse.api.appointment.ContactDetails;
import uk.gov.companieshouse.api.appointment.CorporateIdent;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerSummary;
import uk.gov.companieshouse.api.appointment.OfficerSummary.OfficerRoleEnum;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.CompanyAppointmentsApplication;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;
import uk.gov.companieshouse.company_appointments.roles.RoleHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CompanyAppointmentMapper {

    private static final Pattern REGEX = Pattern.compile("^(?i)(?=m)(?:mrs?|miss|ms|master)$");
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    public OfficerSummary map(CompanyAppointmentDocument companyAppointmentData) {
        return map(companyAppointmentData, false);
    }

    public OfficerSummary map(CompanyAppointmentDocument companyAppointment, boolean registerView) {
        LOGGER.debug("Mapping data for appointment: " + companyAppointment.getId(), DataMapHolder.getLogMap());

        DeltaOfficerData data = Optional.ofNullable(companyAppointment.getData())
                .orElseThrow(() -> new IllegalArgumentException("Missing data element"));

        boolean isSecretary = RoleHelper.isSecretary(companyAppointment);

        LocalDate appointedOn = extractDate(data.getAppointedOn());
        LocalDate appointedBefore = extractDate(data.getAppointedBefore());
        LocalDate resignedOn = extractDate(data.getResignedOn());

        OfficerSummary result = new OfficerSummary()
                .appointedOn(appointedOn)
                .appointedBefore(appointedBefore)
                .resignedOn(resignedOn)
                .countryOfResidence(isSecretary ? null : data.getCountryOfResidence())
                .dateOfBirth(isSecretary ? null : Optional.ofNullable(companyAppointment.getSensitiveData())
                        .map(DeltaSensitiveData::getDateOfBirth)
                        .map(dob -> mapDateOfBirth(dob, registerView))
                        .orElse(null))
                .links(mapLinks(data.getLinks()))
                .nationality(data.getNationality())
                .occupation(data.getOccupation())
                .officerRole(OfficerRoleEnum.fromValue(data.getOfficerRole()))
                .address(mapAddress(data.getServiceAddress()))
                .identification(mapCorporateInfo(data.getIdentification()))
                .formerNames(mapFormerNames(data.getFormerNames()))
                .name(mapOfficerName(data))
                .responsibilities(data.getResponsibilities())
                .principalOfficeAddress(mapPrincipalOfficeAddress(data.getPrincipalOfficeAddress()))
                .contactDetails(mapContactDetails(data.getContactDetails()))
                .isPre1992Appointment(data.getPre1992Appointment())
                .personNumber(data.getPersonNumber());
        LOGGER.debug("Mapped data for appointment: " + companyAppointment.getId(), DataMapHolder.getLogMap());
        return result;
    }

    private static LocalDate extractDate(Instant data) {
        return Optional.ofNullable(data)
                .map(date -> LocalDate.from(date.atZone(UTC)))
                .orElse(null);
    }

    private List<FormerNames> mapFormerNames(List<DeltaFormerNames> names) {
        return Optional.ofNullable(names)
                .map(formerNames -> formerNames.stream().map(
                                formerName -> new FormerNames().forenames(formerName.getForenames())
                                        .surname(formerName.getSurname()))
                        .collect(Collectors.toList())).orElse(null);
    }

    private CorporateIdent mapCorporateInfo(DeltaIdentification deltaIdentification) {
        return Optional.ofNullable(deltaIdentification)
                .map(identification -> new CorporateIdent()
                        .identificationType(
                                CorporateIdent.IdentificationTypeEnum.fromValue(identification.getIdentificationType()))
                        .legalAuthority(identification.getLegalAuthority())
                        .legalForm(identification.getLegalForm())
                        .placeRegistered(identification.getPlaceRegistered())
                        .registrationNumber(identification.getRegistrationNumber()))
                .orElse(null);
    }

    private Address mapAddress(DeltaServiceAddress serviceAddress) {
        return Optional.ofNullable(serviceAddress)
                .map(address -> new Address()
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .careOf(address.getCareOf())
                        .country(address.getCountry())
                        .locality(address.getLocality())
                        .postalCode(address.getPostalCode())
                        .poBox(address.getPoBox())
                        .premises(address.getPremises())
                        .region(address.getRegion()))
                .orElse(null);
    }

    private PrincipalOfficeAddress mapPrincipalOfficeAddress(DeltaPrincipalOfficeAddress principalOfficeAddress) {
        return Optional.ofNullable(principalOfficeAddress)
                .map(address -> new PrincipalOfficeAddress()
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .careOf(address.getCareOf())
                        .country(address.getCountry())
                        .locality(address.getLocality())
                        .postalCode(address.getPostalCode())
                        .poBox(address.getPoBox())
                        .premises(address.getPremises())
                        .region(address.getRegion()))
                .orElse(null);
    }

    private ContactDetails mapContactDetails(DeltaContactDetails deltaContactDetails) {
        return Optional.ofNullable(deltaContactDetails)
                .map(contactDetails -> new ContactDetails()
                        .contactName(contactDetails.getContactName()))
                .orElse(null);
    }

    private ItemLinkTypes mapLinks(DeltaItemLinkTypes itemLinkTypes) {
        return Optional.ofNullable(itemLinkTypes)
                .map(links -> new ItemLinkTypes()
                        .self(links.getSelf())
                        .officer(new OfficerLinkTypes()
                                .appointments(Optional.ofNullable(links.getOfficer())
                                        .map(DeltaOfficerLinkTypes::getAppointments)
                                        .orElse(null))))
                .orElse(null);
    }

    private DateOfBirth mapDateOfBirth(Instant dob, boolean registerView) {
        return registerView ? mapDateOfBirth(dob, dob.atZone(UTC).getDayOfMonth()) : mapDateOfBirth(dob, null);
    }

    private DateOfBirth mapDateOfBirth(Instant dob, Integer day) {
        return new DateOfBirth()
                .day(day)
                .month(dob.atZone(UTC).getMonthValue())
                .year(dob.atZone(UTC).getYear());
    }

    private String mapOfficerName(DeltaOfficerData data) {
        return Optional.ofNullable(data.getCompanyName())
                .orElseGet(() -> individualOfficerName(data));
    }

    private String individualOfficerName(DeltaOfficerData data) {
        String result = data.getSurname();
        if (data.getForename() != null || data.getOtherForenames() != null) {
            result = String.join(", ", data.getSurname(),
                    Stream.of(data.getForename(), data.getOtherForenames())
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" ")));
        }
        if (data.getTitle() != null && !REGEX.matcher(data.getTitle()).matches()) {
            result = String.join(", ", result, data.getTitle());
        }
        return result;
    }
}
