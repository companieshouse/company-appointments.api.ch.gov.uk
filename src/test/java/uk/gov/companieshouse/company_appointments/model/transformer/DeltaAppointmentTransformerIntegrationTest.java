package uk.gov.companieshouse.company_appointments.model.transformer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import uk.gov.companieshouse.api.appointment.*;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;

import java.time.Instant;
import java.time.OffsetDateTime;

@SpringBootTest
@EnableAspectJAutoProxy
class DeltaAppointmentTransformerIntegrationTest {

    @Autowired
    private DeltaAppointmentTransformer transformer;

    @MockBean
    private DeltaOfficerData deltaOfficerData;
    @MockBean
    private DeltaSensitiveData deltaSensitiveData;
    @MockBean
    private DeltaOfficerDataTransformer officerDataTransformer;
    @MockBean
    private DeltaSensitiveDataTransformer sensitiveDataTransformer;

    @BeforeAll
    static void setUp() {
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void successfullyCallAspect() throws Exception {
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

        transformer.transform(fullRecordCompanyOfficerApi);
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
