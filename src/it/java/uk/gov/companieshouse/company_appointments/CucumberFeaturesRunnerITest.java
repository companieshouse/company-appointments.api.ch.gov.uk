package uk.gov.companieshouse.company_appointments;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import uk.gov.companieshouse.company_appointments.config.AbstractIntegrationTest;


@Suite
@SelectClasspathResource("features")
@CucumberContextConfiguration
public class CucumberFeaturesRunnerITest extends AbstractIntegrationTest {
}
