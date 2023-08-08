package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;

@Component
public class DeltaServiceAddressTransformer implements
        Transformative<ServiceAddress, DeltaServiceAddress> {

    @Override
    public DeltaServiceAddress factory() {
        return new DeltaServiceAddress();
    }

    @Override
    public DeltaServiceAddress transform(ServiceAddress source, DeltaServiceAddress entity) {
        entity.setAddressLine1(source.getAddressLine1());
        entity.setAddressLine2(source.getAddressLine2());
        entity.setCareOf(source.getCareOf());
        entity.setCountry(source.getCountry());
        entity.setLocality(source.getLocality());
        entity.setPoBox(source.getPoBox());
        entity.setPostalCode(source.getPostalCode());
        entity.setPremises(source.getPremises());
        entity.setRegion(source.getRegion());
        return entity;
    }
}
