package uk.gov.companieshouse.company_appointments.officerappointments;

import java.util.List;

record Filter(boolean isFilterEnabled, List<String> filterStatuses) {

}
