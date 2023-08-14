package uk.gov.companieshouse.company_appointments.repository;

import java.util.List;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

public interface CompanyAppointmentRepositoryExtension {

    List<CompanyAppointmentDocument> getCompanyAppointments(String companyNumber,
            String orderBy, String registerType, int startIndex, int itemsPerPage,
            boolean registerView, boolean filterEnabled);
}