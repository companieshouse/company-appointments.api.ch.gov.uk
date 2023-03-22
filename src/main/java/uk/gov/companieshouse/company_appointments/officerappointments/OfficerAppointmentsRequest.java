package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.Objects;

public class OfficerAppointmentsRequest {

    private final String officerId;
    private final String filter;
    private final Integer startIndex;
    private final Integer itemsPerPage;

    public OfficerAppointmentsRequest(String officerId, String filter, Integer startIndex, Integer itemsPerPage) {
        this.officerId = officerId;
        this.filter = filter;
        this.startIndex = startIndex;
        this.itemsPerPage = itemsPerPage;
    }

    public String getOfficerId() {
        return officerId;
    }

    public String getFilter() {
        return filter;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OfficerAppointmentsRequest request = (OfficerAppointmentsRequest) o;
        return Objects.equals(officerId, request.officerId)
                && Objects.equals(filter, request.filter)
                && Objects.equals(startIndex, request.startIndex)
                && Objects.equals(itemsPerPage, request.itemsPerPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(officerId, filter, startIndex, itemsPerPage);
    }
}
