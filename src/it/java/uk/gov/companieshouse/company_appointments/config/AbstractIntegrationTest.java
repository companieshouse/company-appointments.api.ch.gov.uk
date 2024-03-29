package uk.gov.companieshouse.company_appointments.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;

/**
 * Loads the application context.
 * Best place to mock your downstream calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    @MockBean
    public CompanyMetricsApiService companyMetricsApiService;
}
