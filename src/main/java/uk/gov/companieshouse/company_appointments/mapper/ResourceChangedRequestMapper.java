package uk.gov.companieshouse.company_appointments.mapper;

import java.time.Instant;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.company_appointments.logging.DataMapHolder;
import uk.gov.companieshouse.company_appointments.model.data.ResourceChangedRequest;
import uk.gov.companieshouse.company_appointments.util.DateTimeProcessor;

@Component
public class ResourceChangedRequestMapper {

    private final DateTimeProcessor dateTimeProcessor;

    public ResourceChangedRequestMapper(DateTimeProcessor dateTimeProcessor) {
        this.dateTimeProcessor = dateTimeProcessor;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResourceEvent event = new ChangedResourceEvent().publishedAt(dateTimeProcessor.formatPublishedAt(
                Instant.now()));
        ChangedResource changedResource = new ChangedResource() //NOSONAR
                .resourceUri(String.format("/company/%s/appointments/%s", request.companyNumber(),
                        request.appointmentId()))
                .resourceKind("company-officers")
                .event(event)
                .contextId(DataMapHolder.getRequestId());

        if (Boolean.TRUE.equals(request.isDelete())) {
            event.setType("deleted");
            changedResource.setDeletedData(request.officersData());
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}
