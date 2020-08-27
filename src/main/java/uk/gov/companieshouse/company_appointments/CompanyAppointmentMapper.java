package uk.gov.companieshouse.company_appointments;

import java.util.stream.Collectors;

import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.company_appointments.model.view.DateOfBirth;
import uk.gov.companieshouse.company_appointments.model.view.FormerNamesView;
import uk.gov.companieshouse.company_appointments.model.view.IdentificationView;
import uk.gov.companieshouse.company_appointments.model.view.LinksView;
import uk.gov.companieshouse.company_appointments.model.view.ServiceAddressView;

public class CompanyAppointmentMapper {

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
        return String.format("%s, %s %s", companyAppointmentData.getData().getSurname(),
                companyAppointmentData.getData().getForename(), companyAppointmentData.getData().getOtherForenames());
    }

}
