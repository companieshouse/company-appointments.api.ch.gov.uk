package uk.gov.companieshouse.company_appointments.model.transformer;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.ContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData.OfficerRoleEnum;

@Component
public class DeltaOfficerDataTransformer implements Transformative<Data, DeltaOfficerData> {

    private final DeltaIdentificationTransformer identificationTransformer;
    private final DeltaItemLinkTypesTransformer itemLinkTypesTransformer;
    private final DeltaPrincipalOfficeAddressTransformer principalOfficeAddressTransformer;
    private final DeltaServiceAddressTransformer serviceAddressTransformer;

    public DeltaOfficerDataTransformer(DeltaIdentificationTransformer identificationTransformer,
            DeltaItemLinkTypesTransformer itemLinkTypesTransformer,
            DeltaPrincipalOfficeAddressTransformer principalOfficeAddressTransformer,
            DeltaServiceAddressTransformer serviceAddressTransformer) {
        this.identificationTransformer = identificationTransformer;
        this.itemLinkTypesTransformer = itemLinkTypesTransformer;
        this.principalOfficeAddressTransformer = principalOfficeAddressTransformer;
        this.serviceAddressTransformer = serviceAddressTransformer;
    }

    @Override
    public DeltaOfficerData factory() {
        return new DeltaOfficerData();
    }

    @Override
    public DeltaOfficerData transform(Data source, DeltaOfficerData entity)
            throws FailedToTransformException {

        try {
            entity.setPersonNumber(source.getPersonNumber());
            entity.setServiceAddress(serviceAddressTransformer.transform(source.getServiceAddress()));
            entity.setServiceAddressSameAsRegisteredOfficeAddress(
                    source.getServiceAddressSameAsRegisteredOfficeAddress());
            entity.setCountryOfResidence(source.getCountryOfResidence());
            entity.setAppointedOn(source.getAppointedOn());
            entity.setAppointedBefore(source.getAppointedBefore());
            entity.setPre1992Appointment(source.getIsPre1992Appointment());
            entity.setLinks(source.getLinks() != null && !source.getLinks().isEmpty()?
                            itemLinkTypesTransformer.transform(source.getLinks().get(0)) : null);
            entity.setNationality(source.getNationality());
            entity.setOccupation(source.getOccupation());
            entity.setOfficerRole(OfficerRoleEnum.fromValue(source.getOfficerRole().getValue()));
            entity.setSecureOfficer(source.getIsSecureOfficer());
            entity.setIdentification(identificationTransformer.transform(source.getIdentification()));
            entity.setCompanyName(source.getCompanyName());
            entity.setSurname(source.getSurname());
            entity.setForename(source.getForename());
            entity.setHonours(source.getHonours());
            entity.setOtherForenames(source.getOtherForenames());
            entity.setTitle(source.getTitle());
            entity.setCompanyNumber(source.getCompanyNumber());
            entity.setContactDetails(new ContactDetails()
                    .contactName(source.getContactDetails().getContactName()));
            entity.setPrincipalOfficeAddress(principalOfficeAddressTransformer.transform(source.getPrincipalOfficeAddress()));
            entity.setResignedOn(source.getResignedOn());
            entity.setResponsibilities(source.getResponsibilities());
            entity.setFormerNames(source.getFormerNames().stream()
                    .map(formerNames -> new DeltaFormerNames()
                            .surname(formerNames.getSurname())
                            .forenames(formerNames.getForenames()))
                    .collect(Collectors.toList()));

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform Data: %s", e.getMessage()));
        }
    }
}
