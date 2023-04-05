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
            entity.setServiceAddress(source.getServiceAddress() != null?
                    serviceAddressTransformer.transform(source.getServiceAddress()) : null);
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
            entity.setOfficerRole(source.getOfficerRole() != null?
                    OfficerRoleEnum.fromValue(source.getOfficerRole().getValue()) : null
            );
            entity.setSecureOfficer(source.getIsSecureOfficer());
            entity.setIdentification(source.getIdentification() != null?
                    identificationTransformer.transform(source.getIdentification()) : null);
            entity.setCompanyName(source.getCompanyName());
            entity.setSurname(source.getSurname());
            entity.setForename(source.getForename());
            entity.setHonours(source.getHonours());
            entity.setOtherForenames(source.getOtherForenames());
            entity.setTitle(source.getTitle());
            entity.setCompanyNumber(source.getCompanyNumber());
            entity.setContactDetails(source.getContactDetails() != null?
                    new ContactDetails()
                            .contactName(source.getContactDetails().getContactName()) : null);
            entity.setPrincipalOfficeAddress(source.getPrincipalOfficeAddress() != null?
                    principalOfficeAddressTransformer.transform(source.getPrincipalOfficeAddress())
                    : null);
            entity.setResignedOn(source.getResignedOn());
            entity.setResponsibilities(source.getResponsibilities());
            entity.setFormerNames(source.getFormerNames() != null?
                    source.getFormerNames().stream()
                    .map(formerNames -> new DeltaFormerNames()
                            .surname(formerNames.getSurname())
                            .forenames(formerNames.getForenames()))
                    .collect(Collectors.toList()) : null);

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform Data: %s", e.getMessage()));
        }
    }
}
