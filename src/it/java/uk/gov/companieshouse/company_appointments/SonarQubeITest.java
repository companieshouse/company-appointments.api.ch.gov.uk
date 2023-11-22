package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonarQubeITest {

    @Test
    void test() {
        // given
        SonarQube sonarQube = new SonarQube();

        // when
        int result = sonarQube.calculate(1,1);

        // then
        assertEquals(2, result);
    }
}
