package uk.gov.companieshouse.company_appointments;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentComparatorTest {

    CompanyAppointmentView companyAppointmentView1;

    CompanyAppointmentView companyAppointmentView2;

    CompanyAppointmentComparator companyAppointmentComparator;

    @BeforeEach
    void setUp() {
        companyAppointmentComparator = new CompanyAppointmentComparator();
        companyAppointmentView1 = new CompanyAppointmentView();
        companyAppointmentView2 = new CompanyAppointmentView();
    }

    @Test
    void testPositiveValueIsReturnedWhenFirstResigned(){
        companyAppointmentView1.setResignedOn(LocalDateTime.of(2020, 12, 22, 0, 0));
        companyAppointmentView2.setResignedOn(null);

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertEquals(1, result);
    }

    @Test
    void testNegativeValueIsReturnedWhenSecondResigned(){
        companyAppointmentView1.setResignedOn(null);
        companyAppointmentView2.setResignedOn(LocalDateTime.of(2020, 12, 22, 0, 0));

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertEquals(-1, result);
    }

    @Test
    void testNegativeValueIsReturnedWhenFirstIsSecretary(){
        companyAppointmentView1.setOfficerRole("secretary");
        companyAppointmentView2.setOfficerRole("director");

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertEquals(-1, result);
    }

    @Test
    void testPositiveValueIsReturnedWhenSecondIsSecretary(){
        companyAppointmentView1.setOfficerRole("director");
        companyAppointmentView2.setOfficerRole("secretary");

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertEquals(1, result);
    }

    @Test
    void testGetNameIsCalledZeroWhenTwoResignedAndSameName(){
        companyAppointmentView1.setResignedOn(LocalDateTime.of(2020, 12, 22, 0, 0));
        companyAppointmentView2.setResignedOn(LocalDateTime.of(2020, 12, 22, 0, 0));
        companyAppointmentView1.setName("ABC");
        companyAppointmentView2.setName("ABC");

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertEquals(0, result);
    }

    @Test
    void testGetNameIsCalledZeroWhenTwoSecretaryAndSameName(){
        companyAppointmentView1.setOfficerRole("secretary");
        companyAppointmentView2.setOfficerRole("secretary");
        companyAppointmentView1.setName("ABC");
        companyAppointmentView2.setName("ABC");

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertEquals(0, result);
    }


    @Test
    void testGetNameIsCalledZeroWhenTwoSecretaryAndTwoResignedAndDifferentName(){
        companyAppointmentView1.setResignedOn(LocalDateTime.of(2020, 12, 22, 0, 0));
        companyAppointmentView2.setResignedOn(LocalDateTime.of(2020, 12, 22, 0, 0));
        companyAppointmentView1.setOfficerRole("secretary");
        companyAppointmentView2.setOfficerRole("secretary");
        companyAppointmentView1.setName("ABC");
        companyAppointmentView2.setName("DEF");

        int result = companyAppointmentComparator.compare(companyAppointmentView1, companyAppointmentView2);
        assertTrue(result<0);
    }
}
