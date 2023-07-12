package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.company_appointments.exception.NotFoundException;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;
import uk.gov.companieshouse.company_appointments.service.DeltaAppointmentTransformerAspect;
import uk.gov.companieshouse.company_appointments.service.CompanyProfileClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeltaAppointmentTransformerAspectTest {

    private static final String COMPANY_NUMBER = "abcdefg";
    private static final String APPOINTMENT_ID = "123456";
    private static final String COMPANY_NAME = "company name";
    private static final String COMPANY_STATUS = "company status";

    private DeltaAppointmentTransformerAspect aspect;

    @Mock
    private CompanyProfileClient companyProfileClient;

    @BeforeEach
    void setUp() {
        aspect = new DeltaAppointmentTransformerAspect(companyProfileClient);
    }

    @Test
    @DisplayName("Successfully populate company name and status fields")
    void populateCompanyNameAndStatus() throws Exception {
        // given
        CompanyAppointmentDocument document = new CompanyAppointmentDocument()
                .setAppointmentId(APPOINTMENT_ID)
                .setCompanyNumber(COMPANY_NUMBER);
        Data data = new Data()
                .companyName(COMPANY_NAME)
                .companyStatus(COMPANY_STATUS);

        when(companyProfileClient.getCompanyProfile(any())).thenReturn(Optional.of(data));

        // when
        aspect.populateCompanyNameAndCompanyStatusFields(document);

        // then
        verify(companyProfileClient).getCompanyProfile(COMPANY_NUMBER);
        assertEquals(COMPANY_NAME, document.getCompanyName());
        assertEquals(COMPANY_STATUS, document.getCompanyStatus());
        assertEquals(APPOINTMENT_ID, document.getAppointmentId());
    }

    @Test
    @DisplayName("Return value is not instance of CompanyAppointmentDocument")
    void methodReturnsEarly() throws Exception {
        // given
        Object invalidObject = new Object();

        // when
        aspect.populateCompanyNameAndCompanyStatusFields(invalidObject);

        // then
        verifyNoInteractions(companyProfileClient);
    }

    @Test
    @DisplayName("Company does not exist in company profile collection")
    void companyProfileClientReturnsNotFound() throws Exception {
        // given
        CompanyAppointmentDocument document = new CompanyAppointmentDocument()
                .setAppointmentId(APPOINTMENT_ID)
                .setCompanyNumber(COMPANY_NUMBER);

        when(companyProfileClient.getCompanyProfile(any())).thenThrow(NotFoundException.class);

        // when
        Executable executable = () -> aspect.populateCompanyNameAndCompanyStatusFields(document);

        // then
        assertThrows(IllegalArgumentException.class, executable);
        verify(companyProfileClient).getCompanyProfile(COMPANY_NUMBER);
    }
}