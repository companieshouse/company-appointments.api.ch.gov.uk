package uk.gov.companieshouse.company_appointments.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import uk.gov.companieshouse.company_appointments.api.CompanyMetricsApiService;

/**
 * Loads the application context.
 * Best place to mock your downstream calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestKafkaConfig.class)
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    public static final ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:latest");

    @MockitoBean
    public CompanyMetricsApiService companyMetricsApiService;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }
}
