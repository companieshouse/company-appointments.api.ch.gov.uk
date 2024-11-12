package uk.gov.companieshouse.company_appointments.api;

import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.company_appointments.exception.ServiceUnavailableException;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;

@SpringBootTest
@ActiveProfiles("feature_flag_enabled")
class ResourceChangedApiServiceAspectFeatureFlagEnabledITest {
    @Autowired
    private ResourceChangedApiService resourceChangedApiService;

    @MockBean
    private ApiClientFactory apiClientFactory;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Test
    void testThatAspectShouldNotProceedWhenFeatureFlagEnabled() throws ServiceUnavailableException {

        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);

        verifyNoInteractions(apiClientFactory);
        verifyNoInteractions(internalApiClient);
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }
}
