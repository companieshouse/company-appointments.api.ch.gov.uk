package uk.gov.companieshouse.company_appointments.model.transformer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;

@ExtendWith(MockitoExtension.class)
class DeltaAppointmentTransformerTest {

    @Mock
    private DeltaOfficerDataTransformer officerDataTransformer;

    @Mock
    private DeltaSensitiveDataTransformer sensitiveDataTransformer;

    @Mock
    private DeltaOfficerData deltaOfficerData;

    @Mock
    private DeltaSensitiveData deltaSensitiveData;

    @InjectMocks
    private DeltaAppointmentTransformer deltaAppointmentTransformer;

    @Test
    void testDeltaIsTransformedSuccessfully() throws FailedToTransformException {
        // given
        CompanyAppointmentDocument expected = new CompanyAppointmentDocument()
                .setId("id")
                .setData(deltaOfficerData)
                .setSensitiveData(deltaSensitiveData)
                .setInternalId("internalId")
                .setAppointmentId("id")
                .setOfficerId("officerId")
                .setPreviousOfficerId("previousOfficerId")
                .setCompanyNumber("companyNumber")
                .setUpdatedBy("updatedBy")
                .setDeltaAt(Instant.parse("2022-01-12T00:00:00.000000Z"))
                .setOfficerRoleSortOrder(22);

        FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi = buildFullRecordOfficer();

        when(officerDataTransformer.transform(any(Data.class))).thenReturn(deltaOfficerData);
        when(sensitiveDataTransformer.transform(
                fullRecordCompanyOfficerApi.getExternalData().getSensitiveData()))
                .thenReturn(deltaSensitiveData);

        // when
        CompanyAppointmentDocument result = deltaAppointmentTransformer.transform(
                fullRecordCompanyOfficerApi);

        // then
        assertThat(result).isEqualTo(expected);
        verify(officerDataTransformer).transform(
                fullRecordCompanyOfficerApi.getExternalData().getData());
        verify(sensitiveDataTransformer).transform(
                fullRecordCompanyOfficerApi.getExternalData().getSensitiveData());
    }

    @Test
    void testApiThrowsExceptionWhenTransformFails() {
        try {
            deltaAppointmentTransformer.transform(new FullRecordCompanyOfficerApi());
            fail("Expected a FailedToTransformException to be thrown");
        } catch (FailedToTransformException e) {
            assert (e.getMessage().contains("Failed to transform API payload:"));
        }
    }

    private FullRecordCompanyOfficerApi buildFullRecordOfficer() {
        FullRecordCompanyOfficerApi output = new FullRecordCompanyOfficerApi();

        ExternalData externalData = new ExternalData();
        Data data = new Data();
        data.setForename("forename");
        data.setSurname("surname");
        data.setOfficerRole(Data.OfficerRoleEnum.DIRECTOR);
        data.setCompanyNumber("companyNumber");
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());
        externalData.setData(data);
        externalData.setSensitiveData(sensitiveData);
        externalData.setAppointmentId("id");
        externalData.setCompanyNumber("companyNumber");
        externalData.setInternalId("internalId");
        externalData.setOfficerId("officerId");
        externalData.setPreviousOfficerId("previousOfficerId");
        InternalData internalData = new InternalData();
        internalData.setOfficerRoleSortOrder(22);
        internalData.setDeltaAt(OffsetDateTime.parse("2022-01-12T00:00:00Z"));
        internalData.setUpdatedBy("updatedBy");
        output.setExternalData(externalData);
        output.setInternalData(internalData);
        return output;
    }
}
