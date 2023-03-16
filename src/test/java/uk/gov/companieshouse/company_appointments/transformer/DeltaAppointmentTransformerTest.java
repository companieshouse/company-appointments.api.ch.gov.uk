package uk.gov.companieshouse.company_appointments.transformer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.transformer.DeltaAppointmentTransformer;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class DeltaAppointmentTransformerTest {

    private DeltaAppointmentTransformer deltaAppointmentTransformer = new DeltaAppointmentTransformer();
    private FullRecordCompanyOfficerApi fullRecordCompanyOfficerApi;

    @Test
    void testDeltaIsTransformedSuccessfully() throws FailedToTransformException{
        // given
        Data data = new Data();
        data.setOfficerRole(Data.OfficerRoleEnum.DIRECTOR);
        data.setCompanyNumber("companyNumber");
        data.setForename("forename");
        data.setSurname("surname");
        SensitiveData sensitiveData = new SensitiveData();
        sensitiveData.setDateOfBirth(new DateOfBirth());
        fullRecordCompanyOfficerApi = buildFullRecordOfficer();
        DeltaAppointmentApi expectedDeltaApi = new DeltaAppointmentApi("id", "etag", data, sensitiveData, "internalId", "id", "officerId",
                "previousOfficerId", "companyNumber", null,"updatedBy", null, "2022-01-12T00:00Z", 22);

        // when
        DeltaAppointmentApi result = deltaAppointmentTransformer.transform(fullRecordCompanyOfficerApi);

        // then
        assertThat(result.getData(), is(expectedDeltaApi.getData()));
        assertThat(result.getDeltaAt(), is(expectedDeltaApi.getDeltaAt()));
        assertThat(result.getUpdatedAt(), is(expectedDeltaApi.getUpdatedAt()));
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
