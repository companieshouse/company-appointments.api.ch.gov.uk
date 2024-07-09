package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class OfficerAppointments {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public OfficerAppointments ids(List<String> ids) {
        this.ids = ids;
        return this;
    }
}
