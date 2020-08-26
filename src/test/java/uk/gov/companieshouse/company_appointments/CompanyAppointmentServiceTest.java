package uk.gov.companieshouse.company_appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CompanyAppointmentServiceTest {

    private CompanyAppointmentService companyAppointmentService;

    @Mock
    private CompanyAppointmentRepository companyAppointmentRepository;

    @Mock
    private CompanyAppointmentMapper companyAppointmentMapper;
    
    @Mock
    private CompanyAppointmentData companyAppointmentData;

    @Mock
    private CompanyAppointment companyAppointment;

    private final static String COMPANY_NUMBER = "123456";

    private final static String APPOINTMENT_ID = "345678";

    @BeforeEach
    void setUp() {
        companyAppointmentService = new CompanyAppointmentService(companyAppointmentRepository,
                companyAppointmentMapper);
    }
    
    @Test
    void testFetchAppointmentReturnsMappedAppointmentData() {
        // given
        when(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID))
                .thenReturn(companyAppointmentData);

        when(companyAppointmentMapper.map(companyAppointmentData)).thenReturn(companyAppointment);

        // when
        CompanyAppointment result = companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APPOINTMENT_ID);

        // then
        assertEquals(companyAppointment, result);
        verify(companyAppointmentRepository).readByCompanyNumberAndAppointmentID(COMPANY_NUMBER, APPOINTMENT_ID);
        verify(companyAppointmentMapper).map(companyAppointmentData);
    }
}
