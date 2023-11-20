package uk.gov.companieshouse.company_appointments;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.company_appointments.controller.SonarQubeSpike;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonarQubeSpikeITest {

    @Test
    void test() {
        // given
        SonarQubeSpike sonarQubeSpike = new SonarQubeSpike();

        // when
        int result = sonarQubeSpike.calculate(1, 1);

        // then
        assertEquals(2, result);
    }
}
