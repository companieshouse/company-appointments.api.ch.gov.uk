package uk.gov.companieshouse.company_appointments.model.transformer;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;

@Component
public class DeltaOfficerDataTransformer implements Transformative<Data, DeltaOfficerData> {

    private final DeltaIdentificationTransformer identificationTransformer;
    private final DeltaItemLinkTypesTransformer itemLinkTypesTransformer;
    private final DeltaPrincipalOfficeAddressTransformer principalOfficeAddressTransformer;
    private final DeltaServiceAddressTransformer serviceAddressTransformer;
    private final DeltaIdentityVerificationDetailsTransformer identityVerificationDetailsTransformer;

    public DeltaOfficerDataTransformer(DeltaIdentificationTransformer identificationTransformer,
                                       DeltaItemLinkTypesTransformer itemLinkTypesTransformer,
                                       DeltaPrincipalOfficeAddressTransformer principalOfficeAddressTransformer,
                                       DeltaServiceAddressTransformer serviceAddressTransformer,
                                       DeltaIdentityVerificationDetailsTransformer identityVerificationDetailsTransformer) {
        this.identificationTransformer = identificationTransformer;
        this.itemLinkTypesTransformer = itemLinkTypesTransformer;
        this.principalOfficeAddressTransformer = principalOfficeAddressTransformer;
        this.serviceAddressTransformer = serviceAddressTransformer;
        this.identityVerificationDetailsTransformer = identityVerificationDetailsTransformer;
    }

    @Override
    public DeltaOfficerData factory() {
        return new DeltaOfficerData();
    }

    @Override
    public DeltaOfficerData transform(Data source, DeltaOfficerData entity)
            throws FailedToTransformException {

        try {
            entity.setEtag(GenerateEtagUtil.generateEtag());
            entity.setPersonNumber(source.getPersonNumber());
            entity.setServiceAddress(source.getServiceAddress() != null?
                    serviceAddressTransformer.transform(source.getServiceAddress()) : null);
            entity.setServiceAddressIsSameAsRegisteredOfficeAddress(
                    source.getServiceAddressIsSameAsRegisteredOfficeAddress());
            entity.setCountryOfResidence(source.getCountryOfResidence());
            entity.setAppointedOn(source.getAppointedOn() != null ?
                    Instant.from(source.getAppointedOn().atStartOfDay(UTC)) : null);
            entity.setAppointedBefore(source.getAppointedBefore() != null ?
                    Instant.from(source.getAppointedBefore().atStartOfDay(UTC)): null);
            entity.setPre1992Appointment(source.getIsPre1992Appointment());
            entity.setLinks(source.getLinks() != null && !source.getLinks().isEmpty()?
                            itemLinkTypesTransformer.transform(source.getLinks().getFirst()) : null);
            entity.setNationality(source.getNationality());
            entity.setOccupation(source.getOccupation());
            entity.setOfficerRole(source.getOfficerRole() != null?
                    source.getOfficerRole().getValue() : null
            );
            entity.setSecureOfficer(source.getIsSecureOfficer());
            entity.setIdentification(source.getIdentification() != null?
                    identificationTransformer.transform(source.getIdentification()) : null);
            entity.setIdentityVerificationDetails(source.getIdentityVerificationDetails() != null ?
                    identityVerificationDetailsTransformer.transform(source.getIdentityVerificationDetails()) : null);
            entity.setCompanyName(source.getCompanyName());
            entity.setSurname(source.getSurname());
            entity.setForename(source.getForename());
            entity.setHonours(source.getHonours());
            entity.setOtherForenames(source.getOtherForenames());
            entity.setTitle(source.getTitle());
            entity.setCompanyNumber(source.getCompanyNumber());
            entity.setContactDetails(source.getContactDetails() != null?
                    new DeltaContactDetails()
                            .setContactName(source.getContactDetails().getContactName()) : null);
            entity.setPrincipalOfficeAddress(source.getPrincipalOfficeAddress() != null?
                    principalOfficeAddressTransformer.transform(source.getPrincipalOfficeAddress())
                    : null);
            entity.setResignedOn(source.getResignedOn() != null ?
                    Instant.from(source.getResignedOn().atStartOfDay(UTC)) : null);
            entity.setResponsibilities(source.getResponsibilities());
            entity.setFormerNames(source.getFormerNames() != null?
                    source.getFormerNames().stream()
                    .map(formerNames -> new DeltaFormerNames()
                            .setSurname(formerNames.getSurname())
                            .setForenames(formerNames.getForenames()))
                    .collect(Collectors.toList()) : null);

            return entity;
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform Data: %s", e.getMessage()));
        }
    }
}
