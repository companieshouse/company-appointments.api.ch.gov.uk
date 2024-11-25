package uk.gov.companieshouse.company_appointments.api;

import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;

@SpringBootTest
class ResourceChangedApiServiceAspectFeatureFlagEnabledITest {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("feature.seeding_collection_enabled", () -> true);
    }

    @Autowired
    private ResourceChangedApiService resourceChangedApiService;

    @Mock
    private ResourceChangedRequestMapper mapper;
    @Mock
    private InternalApiClient chsKafkaApiClient;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;
    @Mock
    private ChangedResource changedResource;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Test
    void testThatAspectShouldNotProceedWhenFeatureFlagEnabled() throws ServiceUnavailableException {
        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        verifyNoInteractions(chsKafkaApiClient);
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(mapper);
        verifyNoInteractions(changedResourcePost);
        verifyNoInteractions(changedResource);
    }
}
