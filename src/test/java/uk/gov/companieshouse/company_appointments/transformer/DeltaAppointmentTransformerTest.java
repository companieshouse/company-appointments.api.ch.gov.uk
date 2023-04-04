package uk.gov.companieshouse.company_appointments.transformer;

import org.junit.Assert;
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
import uk.gov.companieshouse.company_appointments.model.data.DeltaAppointmentApi;
import uk.gov.companieshouse.company_appointments.model.data.DeltaDateOfBirth;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;

import java.time.OffsetDateTime;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaOfficerDataTransformer;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaSensitiveDataTransformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeltaAppointmentTransformerTest {

    @Mock
    private DeltaOfficerDataTransformer officerDataTransformer;

    @Mock
    private DeltaSensitiveDataTransformer sensitiveDataTransformer;

    @InjectMocks
    private DeltaAppointmentTransformer deltaAppointmentTransformer;

    private FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi;

    @Test
    void testDeltaIsTransformedSuccessfully() throws FailedToTransformException{
        // given
        DeltaOfficerData data = new DeltaOfficerData();
        data.setOfficerRole(DeltaOfficerData.OfficerRoleEnum.DIRECTOR);
        data.setCompanyNumber("companyNumber");
        data.setForename("forename");
        data.setSurname("surname");
        DeltaSensitiveData sensitiveData = new DeltaSensitiveData();
        sensitiveData.setDateOfBirth(new DeltaDateOfBirth());
        DeltaAppointmentApi expectedDeltaApi = new DeltaAppointmentApi("id", "etag", data, sensitiveData, "internalId", "id", "officerId",
                "previousOfficerId", "companyNumber", null,"updatedBy", null, "2022-01-12T00:00Z", 22);

        fullRecordCompanyOfficerApi = buildFullRecordOfficer();

        when(officerDataTransformer.transform(fullRecordCompanyOfficerApi.getExternalData().getData()))
                .thenReturn(new DeltaOfficerData());
        when(sensitiveDataTransformer.transform(fullRecordCompanyOfficerApi.getExternalData().getSensitiveData()))
                .thenReturn(new DeltaSensitiveData());

        // when
        DeltaAppointmentApi result = deltaAppointmentTransformer.transform(fullRecordCompanyOfficerApi);

        // then
        assertThat(result.getData(), is(expectedDeltaApi.getData()));
        assertThat(result.getDeltaAt(), is(expectedDeltaApi.getDeltaAt()));
        assertThat(result.getUpdated(), is(expectedDeltaApi.getUpdated()));
        assertThat(result.getUpdatedBy(), is(expectedDeltaApi.getUpdatedBy()));
    }

    @Test
    void testApiThrowsExceptionWhenTransformFails() {
        try {
            deltaAppointmentTransformer.transform(new FullRecordCompanyOfficerApi());
            Assert.fail("Expected a FailedToTransformException to be thrown");
        } catch (FailedToTransformException e) {
            assert(e.getMessage().contains("Failed to transform API payload:"));
        }
    }

    private FullRecordCompanyOfficerApi buildFullRecordOfficer() {
        FullRecordCompanyOfficerApi output  = new FullRecordCompanyOfficerApi();

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
