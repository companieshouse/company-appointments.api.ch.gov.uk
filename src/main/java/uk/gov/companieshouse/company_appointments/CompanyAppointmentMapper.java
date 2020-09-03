package uk.gov.companieshouse.company_appointments;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.data.OfficerLinksData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirth;
import uk.gov.companieshouse.company_appointments.model.view.FormerNamesView;
import uk.gov.companieshouse.company_appointments.model.view.IdentificationView;
import uk.gov.companieshouse.company_appointments.model.view.LinksView;
import uk.gov.companieshouse.company_appointments.model.view.ServiceAddressView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CompanyAppointmentMapper {

    private static final String REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAppointmentsApplication.APPLICATION_NAMESPACE);

    public CompanyAppointmentView map(CompanyAppointmentData companyAppointmentData) {
        LOGGER.debug("Mapping data for appointment: " + companyAppointmentData.getId());
        boolean isSecretary = SecretarialRoles.stream().anyMatch(s -> s.getRole().equals(companyAppointmentData.getData().getOfficerRole()));

        CompanyAppointmentView result = CompanyAppointmentView.builder()
                .withAppointedOn(companyAppointmentData.getData().getAppointedOn())
                .withResignedOn(companyAppointmentData.getData().getResignedOn())
                .withCountryOfResidence(isSecretary ? null : companyAppointmentData.getData().getCountryOfResidence())
                .withDateOfBirth(isSecretary ? null : mapDateOfBirth(companyAppointmentData))
                .withLinks(mapLinks(companyAppointmentData))
                .withNationality(companyAppointmentData.getData().getNationality())
                .withOccupation(companyAppointmentData.getData().getOccupation())
                .withOfficerRole(companyAppointmentData.getData().getOfficerRole())
                .withServiceAddress(mapServiceAddress(companyAppointmentData))
                .withIdentification(mapCorporateInfo(companyAppointmentData))
                .withFormerNames(mapFormerNames(companyAppointmentData))
                .withName(mapOfficerName(companyAppointmentData))
                .build();
        LOGGER.debug("Mapped data for appointment: " + companyAppointmentData.getId());
        return result;
    }

    private List<FormerNamesView> mapFormerNames(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getFormerNameData())
                .map(formerNames -> formerNames.stream().map(
                        formerName -> new FormerNamesView(formerName.getForenames(), formerName.getSurname()))
                        .collect(Collectors.toList())).orElse(null);
    }

    private IdentificationView mapCorporateInfo(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getIdentificationData())
                .map(corporateData -> IdentificationView.builder()
                        .withIdentificationType(corporateData.getIdentificationType())
                        .withLegalAuthority(corporateData.getLegalAuthority())
                        .withLegalForm(corporateData.getLegalForm())
                        .withPlaceRegistered(corporateData.getPlaceRegistered())
                        .withRegistrationNumber(corporateData.getRegistrationNumber())
                        .build())
                .orElse(null);
    }

    private ServiceAddressView mapServiceAddress(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getServiceAddress())
                .map(address -> ServiceAddressView.builder()
                        .withAddressLine1(address.getAddressLine1())
                        .withAddressLine2(address.getAddressLine2())
                        .withCareOf(address.getCareOf())
                        .withCountry(address.getCountry())
                        .withLocality(address.getLocality())
                        .withPostcode(address.getPostcode())
                        .withPoBox(address.getPoBox())
                        .withPremises(address.getPremises())
                        .withRegion(address.getRegion())
                        .build()).orElse(null);
    }

    private LinksView mapLinks(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getLinksData())
                .map(links -> new LinksView(links.getSelfLink(),
                        Optional.ofNullable(links.getOfficerLinksData())
                                .map(OfficerLinksData::getAppointmentsLink).orElse(null)))
                .orElse(null);
    }

    private DateOfBirth mapDateOfBirth(CompanyAppointmentData companyAppointmentData) {
        return Optional.ofNullable(companyAppointmentData.getData().getDateOfBirth())
                .map(dateOfBirth -> new DateOfBirth(
                        dateOfBirth.getMonthValue(),
                        dateOfBirth.getYear()))
                .orElse(null);
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
