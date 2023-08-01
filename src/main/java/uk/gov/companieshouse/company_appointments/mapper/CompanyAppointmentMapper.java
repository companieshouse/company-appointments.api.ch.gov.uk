package uk.gov.companieshouse.company_appointments.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
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
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerLinksData;
import uk.gov.companieshouse.company_appointments.roles.RoleHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CompanyAppointmentMapper {

    private static final String REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);
    private static final String APPOINTED_BEFORE_DATE_FORMAT = "yyyy-MM-dd";

    public OfficerSummary map(CompanyAppointmentData companyAppointmentData) {
        return map(companyAppointmentData, false);
    }

    public OfficerSummary map(CompanyAppointmentData companyAppointmentData, boolean registerView) {
        LOGGER.debug("Mapping data for appointment: " + companyAppointmentData.getId());
        boolean isSecretary = RoleHelper.isSecretary(companyAppointmentData);
        LocalDateTime appointedOn = companyAppointmentData.getData().getAppointedOn();
        String appointedBefore = companyAppointmentData.getData().getAppointedBefore();
        LocalDateTime resignedOn = companyAppointmentData.getData().getResignedOn();

        OfficerSummary result = new OfficerSummary()
                .appointedOn(appointedOn == null ? null : appointedOn.toLocalDate())
                .appointedBefore(StringUtils.isBlank(appointedBefore) ? null : LocalDate.parse(
                        appointedBefore,
                        DateTimeFormatter.ofPattern(APPOINTED_BEFORE_DATE_FORMAT)))
                .resignedOn(resignedOn == null ? null : resignedOn.toLocalDate())
                .countryOfResidence(isSecretary ? null : companyAppointmentData.getData().getCountryOfResidence())
                .dateOfBirth(isSecretary ? null : mapDateOfBirth(companyAppointmentData, registerView))
                .links(mapLinks(companyAppointmentData))
                .nationality(companyAppointmentData.getData().getNationality())
                .occupation(companyAppointmentData.getData().getOccupation())
                .officerRole(OfficerRoleEnum.fromValue(companyAppointmentData.getData().getOfficerRole()))
                .address(mapAddress(companyAppointmentData))
                .identification(mapCorporateInfo(companyAppointmentData))
                .formerNames(mapFormerNames(companyAppointmentData))
                .name(mapOfficerName(companyAppointmentData))
                .responsibilities(companyAppointmentData.getData().getResponsibilities())
                .principalOfficeAddress(mapPrincipalOfficeAddress(companyAppointmentData))
                .contactDetails(mapContactDetails(companyAppointmentData))
                .isPre1992Appointment(companyAppointmentData.getData().getIsPre1992Appointment());
                // TODO: Map person number when switching to delta_appointments collection
        LOGGER.debug("Mapped data for appointment: " + companyAppointmentData.getId());
        return result;
    }

    private List<FormerNames> mapFormerNames(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getFormerNameData())
                .map(formerNames -> formerNames.stream().map(
                        formerName -> new FormerNames().forenames(formerName.getForenames()).surname(formerName.getSurname()))
                        .collect(Collectors.toList())).orElse(null);
    }

    private CorporateIdent mapCorporateInfo(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getIdentificationData())
                .map(corporateData -> new CorporateIdent()
                        .identificationType(CorporateIdent.IdentificationTypeEnum.fromValue(corporateData.getIdentificationType()))
                        .legalAuthority(corporateData.getLegalAuthority())
                        .legalForm(corporateData.getLegalForm())
                        .placeRegistered(corporateData.getPlaceRegistered())
                        .registrationNumber(corporateData.getRegistrationNumber()))
                .orElse(null);
    }

    private Address mapAddress(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getServiceAddress())
                .map(address -> new Address()
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .careOf(address.getCareOf())
                        .country(address.getCountry())
                        .locality(address.getLocality())
                        .postalCode(address.getPostcode())
                        .poBox(address.getPoBox())
                        .premises(address.getPremises())
                        .region(address.getRegion()))
                .orElse(null);
    }

    private PrincipalOfficeAddress mapPrincipalOfficeAddress(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getPrincipalOfficeAddress())
                .map(address -> new PrincipalOfficeAddress()
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .careOf(address.getCareOf())
                        .country(address.getCountry())
                        .locality(address.getLocality())
                        .postalCode(address.getPostcode())
                        .poBox(address.getPoBox())
                        .premises(address.getPremises())
                        .region(address.getRegion()))
                .orElse(null);
    }

    private ContactDetails mapContactDetails(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getContactDetails())
                .map(contactDetails -> new ContactDetails()
                        .contactName(contactDetails.getContactName()))
                .orElse(null);
    }

    private ItemLinkTypes mapLinks(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getLinksData())
                .map(links -> new ItemLinkTypes()
                        .self(links.getSelfLink())
                        .officer(
                            new OfficerLinkTypes()
                                .appointments(Optional.ofNullable(links.getOfficerLinksData())
                                    .map(OfficerLinksData::getAppointmentsLink).orElse(null))))
                .orElse(null);
    }

    private DateOfBirth mapDateOfBirth(CompanyAppointmentData companyAppointmentData, boolean registerView) {
        if (registerView) {
            return Optional.ofNullable(companyAppointmentData.getData().getDateOfBirth())
                    .map(dateOfBirth -> new DateOfBirth()
                            .day(dateOfBirth.getDayOfMonth())
                            .month(dateOfBirth.getMonthValue())
                            .year(dateOfBirth.getYear()))
                    .orElse(null);
        } else {
            return Optional.ofNullable(companyAppointmentData.getData().getDateOfBirth())
                    .map(dateOfBirth -> new DateOfBirth()
                            .month(dateOfBirth.getMonthValue())
                            .year(dateOfBirth.getYear()))
                    .orElse(null);
        }
    }

    private String mapOfficerName(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getCompanyName())
                .orElseGet(() -> this.individualOfficerName(companyAppointmentData));
    }

    private String individualOfficerName(CompanyAppointmentData companyAppointmentData) {
        String result = companyAppointmentData.getData().getSurname();
        if (companyAppointmentData.getData().getForename() != null || companyAppointmentData.getData().getOtherForenames() != null) {
            result = String.join(", ", companyAppointmentData.getData().getSurname(), Stream.of(companyAppointmentData.getData().getForename(), companyAppointmentData.getData().getOtherForenames())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")));
        }
        if (companyAppointmentData.getData().getTitle() != null && !companyAppointmentData.getData().getTitle().matches(REGEX)) {
            result = String.join(", ", result, companyAppointmentData.getData().getTitle());
        }
        return result;
    }
}
