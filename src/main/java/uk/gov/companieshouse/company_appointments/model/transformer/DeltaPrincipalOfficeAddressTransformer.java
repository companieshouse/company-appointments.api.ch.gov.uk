package uk.gov.companieshouse.company_appointments.model.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;

@Component
public class DeltaPrincipalOfficeAddressTransformer implements Transformative<PrincipalOfficeAddress,
        DeltaPrincipalOfficeAddress> {

    @Override
    public DeltaPrincipalOfficeAddress factory() {
        return new DeltaPrincipalOfficeAddress();
    }

    @Override
    public DeltaPrincipalOfficeAddress transform(PrincipalOfficeAddress source,
            DeltaPrincipalOfficeAddress entity) throws FailedToTransformException {

        try {
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
        } catch (Exception e) {
            throw new FailedToTransformException(String.format("Failed to transform PrincipalOfficeAddress: %s", e.getMessage()));
        }
    }
}
