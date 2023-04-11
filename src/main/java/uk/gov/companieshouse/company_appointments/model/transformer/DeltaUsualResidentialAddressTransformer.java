package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaUsualResidentialAddress;

@Component
public class DeltaUsualResidentialAddressTransformer implements
        Transformative<UsualResidentialAddress, DeltaUsualResidentialAddress> {

    @Override
    public DeltaUsualResidentialAddress factory() {
        return new DeltaUsualResidentialAddress();
    }

    @Override
    public DeltaUsualResidentialAddress transform(UsualResidentialAddress source, DeltaUsualResidentialAddress entity) {
        entity.setAddressLine1(source.getAddressLine1());
        entity.setAddressLine2(source.getAddressLine2());
        entity.setCountry(source.getCountry());
        entity.setLocality(source.getLocality());
        entity.setPostalCode(source.getPostalCode());
        entity.setPremises(source.getPremises());
        entity.setRegion(source.getRegion());
        entity.setCareOf(source.getCareOf());
        entity.setPoBox(source.getPoBox());
        return entity;
    }
}
