package uk.gov.companieshouse.company_appointments.mapper;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;

@Component
public class ResourceChangedRequestMapper {

    private final Supplier<String> timestampGenerator;

    public ResourceChangedRequestMapper(Supplier<String> timestampGenerator) {
        this.timestampGenerator = timestampGenerator;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResourceEvent event = new ChangedResourceEvent().publishedAt(this.timestampGenerator.get());
        ChangedResource changedResource = new ChangedResource() //NOSONAR
                .resourceUri(String.format("/company/%s/appointments/%s", request.companyNumber(),
                        request.appointmentId()))
                .resourceKind("company-officers")
                .event(event)
                .contextId(request.contextId());

        if (Boolean.TRUE.equals(request.isDelete())) {
            event.setType("deleted");
            changedResource.setDeletedData(request.officersData());
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}
