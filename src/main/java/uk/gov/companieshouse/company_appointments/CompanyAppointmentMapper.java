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

@Component
public class CompanyAppointmentMapper {

    private static final String REGEX = "^(?:(?:[Mm]rs?)|(?:[Mm]iss)|(?:[Mm]s)|(?:[Mm]aster))$";

    public CompanyAppointmentView map(CompanyAppointmentData companyAppointmentData) {
        return CompanyAppointmentView.builder()
                .withAppointedOn(companyAppointmentData.getData().getAppointedOn())
                .withResignedOn(companyAppointmentData.getData().getResignedOn())
                .withCountryOfResidence(companyAppointmentData.getData().getCountryOfResidence())
                .withDateOfBirth(mapDateOfBirth(companyAppointmentData))
                .withLinks(mapLinks(companyAppointmentData))
                .withNationality(companyAppointmentData.getData().getNationality())
                .withOccupation(companyAppointmentData.getData().getOccupation())
                .withOfficerRole(companyAppointmentData.getData().getOfficerRole())
                .withServiceAddress(mapServiceAddress(companyAppointmentData))
                .withIdentification(mapCorporateInfo(companyAppointmentData))
                .withFormerNames(mapFormerNames(companyAppointmentData))
                .withName(mapOfficerName(companyAppointmentData))
                .build();
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
                        dateOfBirth.getDayOfMonth(),
                        dateOfBirth.getMonthValue(),
                        dateOfBirth.getYear()))
                .orElse(null);
    }

    private String mapOfficerName(CompanyAppointmentData companyAppointmentData) {
        List<String> forenames = Stream.of(companyAppointmentData.getData().getForename(), companyAppointmentData.getData().getOtherForenames())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String result = companyAppointmentData.getData().getSurname();
        if (!forenames.isEmpty()) {
            result = String.join(", ", companyAppointmentData.getData().getSurname(), String.join(" ", forenames));
        }
        if (companyAppointmentData.getData().getTitle() != null && !companyAppointmentData.getData().getTitle().matches(REGEX)) {
            result = String.join(", ", result, companyAppointmentData.getData().getTitle());
        }
        return result;
    }

}
