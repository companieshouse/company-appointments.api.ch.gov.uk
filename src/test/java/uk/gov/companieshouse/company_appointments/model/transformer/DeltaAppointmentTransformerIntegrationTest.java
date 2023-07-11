package uk.gov.companieshouse.company_appointments.model.transformer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaSensitiveData;
import uk.gov.companieshouse.company_appointments.service.CompanyProfileClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
class DeltaAppointmentTransformerIntegrationTest {

    private static final String COMPANY_NAME = "companyName";
    private static final String COMPANY_STATUS = "companyStatus";
    private static final String COMPANY_NUMBER = "12345678";

    @Autowired
    private DeltaAppointmentTransformer transformer;
    @Autowired
    private DeltaItemLinkTypesTransformer itemLinkTypesTransformer;

    @Mock
    private DeltaOfficerData deltaOfficerData;
    @Mock
    private DeltaSensitiveData deltaSensitiveData;
    @Mock
    private uk.gov.companieshouse.api.company.Data companyProfileData;

    @MockBean
    private DeltaOfficerDataTransformer officerDataTransformer;
    @MockBean
    private DeltaSensitiveDataTransformer sensitiveDataTransformer;
    @MockBean
    private CompanyProfileClient client;

    @BeforeAll
    static void setUp() {
        System.setProperty("company-metrics-api.endpoint", "localhost");
    }

    @Test
    void successfullyCallAspect() throws Exception {
        // given
        when(officerDataTransformer.transform(any(Data.class))).thenReturn(deltaOfficerData);
        when(sensitiveDataTransformer.transform(any(SensitiveData.class))).thenReturn(deltaSensitiveData);
        when(companyProfileData.getCompanyName()).thenReturn(COMPANY_NAME);
        when(companyProfileData.getCompanyStatus()).thenReturn(COMPANY_STATUS);
        when(client.getCompanyProfile(anyString())).thenReturn(Optional.of(companyProfileData));

        CompanyAppointmentDocument expected = new CompanyAppointmentDocument()
                .setId("id")
                .setData(deltaOfficerData)
                .setSensitiveData(deltaSensitiveData)
                .setInternalId("internalId")
                .setAppointmentId("id")
                .setOfficerId("officerId")
                .setPreviousOfficerId("previousOfficerId")
                .setCompanyNumber(COMPANY_NUMBER)
                .setUpdatedBy("updatedBy")
                .setDeltaAt(Instant.parse("2022-01-12T00:00:00.000000Z"))
                .setOfficerRoleSortOrder(22)
                .setCompanyName(COMPANY_NAME)
                .setCompanyStatus(COMPANY_STATUS);

        // when
        CompanyAppointmentDocument actual = transformer.transform(buildFullRecordOfficer());

        // then
        verify(client).getCompanyProfile(COMPANY_NUMBER);
        assertEquals(expected, actual);
    }

    @Test
    void aspectNotAppliedInvalidReturnType() throws Exception {
        // given

        // when
        itemLinkTypesTransformer.transform(new ItemLinkTypes());

        // then
        verifyNoInteractions(client);
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
        externalData.setCompanyNumber("12345678");
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

