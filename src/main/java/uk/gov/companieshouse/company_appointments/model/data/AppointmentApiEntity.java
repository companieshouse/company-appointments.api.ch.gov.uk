package uk.gov.companieshouse.company_appointments.model.data;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;

@Document(collection = "delta_appointments")
public class AppointmentApiEntity extends AppointmentAPI {

    public AppointmentApiEntity(final AppointmentAPI appointmentAPI) {
        super(appointmentAPI.getId(), appointmentAPI.getData(), appointmentAPI.getInternalId(),
            appointmentAPI.getAppointmentId(), appointmentAPI.getOfficerId(),
            appointmentAPI.getPreviousOfficerId(), appointmentAPI.getDeltaAt());
    }

}
