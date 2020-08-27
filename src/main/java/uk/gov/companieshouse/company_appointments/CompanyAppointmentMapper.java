package uk.gov.companieshouse.company_appointments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirth;
import uk.gov.companieshouse.company_appointments.model.view.FormerNamesView;
import uk.gov.companieshouse.company_appointments.model.view.IdentificationView;
import uk.gov.companieshouse.company_appointments.model.view.LinksView;
import uk.gov.companieshouse.company_appointments.model.view.ServiceAddressView;

public class CompanyAppointmentMapper {

    private static final String REGEX = "^(?:(?:[Mm]rs?)|(?:[Mm]iss)|(?:[Mm]s)|(?:[Mm]aster))$";

    public CompanyAppointmentView map(CompanyAppointmentData companyAppointmentData) {
        return CompanyAppointmentView.builder()
                .withAppointedOn(companyAppointmentData.getData().getAppointedOn())
                .withResignedOn(companyAppointmentData.getData().getResignedOn())
                .withCountryOfResidence(companyAppointmentData.getData().getCountryOfResidence())
                .withDateOfBirth(new DateOfBirth(
                        companyAppointmentData.getData().getDateOfBirth().getDayOfMonth(),
                        companyAppointmentData.getData().getDateOfBirth().getMonthValue(),
                        companyAppointmentData.getData().getDateOfBirth().getYear()))
                .withLinks(new LinksView(companyAppointmentData.getData().getLinksData().getSelfLink(),
                        companyAppointmentData.getData().getLinksData().getOfficerLinksData().getAppointmentsLink()))
                .withNationality(companyAppointmentData.getData().getNationality())
                .withOccupation(companyAppointmentData.getData().getOccupation())
                .withOfficerRole(companyAppointmentData.getData().getOfficerRole())
                .withServiceAddress(ServiceAddressView.builder()
                        .withAddressLine1(companyAppointmentData.getData().getServiceAddress().getAddressLine1())
                        .withAddressLine2(companyAppointmentData.getData().getServiceAddress().getAddressLine2())
                        .withCareOf(companyAppointmentData.getData().getServiceAddress().getCareOf())
                        .withCountry(companyAppointmentData.getData().getServiceAddress().getCountry())
                        .withLocality(companyAppointmentData.getData().getServiceAddress().getLocality())
                        .withPostcode(companyAppointmentData.getData().getServiceAddress().getPostcode())
                        .withPoBox(companyAppointmentData.getData().getServiceAddress().getPoBox())
                        .withPremises(companyAppointmentData.getData().getServiceAddress().getPremises())
                        .withRegion(companyAppointmentData.getData().getServiceAddress().getRegion())
                        .build())
                .withIdentification(IdentificationView.builder()
                        .withIdentificationType(companyAppointmentData.getData().getIdentificationData().getIdentificationType())
                        .withLegalAuthority(companyAppointmentData.getData().getIdentificationData().getLegalAuthority())
                        .withLegalForm(companyAppointmentData.getData().getIdentificationData().getLegalForm())
                        .withPlaceRegistered(companyAppointmentData.getData().getIdentificationData().getPlaceRegistered())
                        .withRegistrationNumber(companyAppointmentData.getData().getIdentificationData().getRegistrationNumber())
                        .build())
                .withFormerNames(
                        companyAppointmentData.getData().getFormerNameData().stream().map(
                                formerName -> new FormerNamesView(formerName.getForenames(), formerName.getSurname()))
                                .collect(Collectors.toList()))
                .withName(formatOfficerName(companyAppointmentData))
                .build();
    }

    private String formatOfficerName(CompanyAppointmentData companyAppointmentData) {
        List<String> forenames = Stream.of(companyAppointmentData.getData().getForename(), companyAppointmentData.getData().getOtherForenames())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String result = companyAppointmentData.getData().getSurname();
        if(!forenames.isEmpty()) {
            result = String.join(", ", companyAppointmentData.getData().getSurname(), String.join(" ", forenames));
        }
        if(companyAppointmentData.getData().getTitle() != null && !companyAppointmentData.getData().getTitle().matches(REGEX)){
            result = String.join(", ", result, companyAppointmentData.getData().getTitle());
        }
        return result;
    }

}
