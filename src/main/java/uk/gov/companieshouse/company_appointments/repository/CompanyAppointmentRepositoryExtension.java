package uk.gov.companieshouse.company_appointments.repository;

import java.util.List;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

public interface CompanyAppointmentRepositoryExtension {

  List<CompanyAppointmentData> getCompanyAppointmentData(String companyNumber,
          String orderBy, String registerType, int startIndex, int itemsPerPage,
          boolean registerView, boolean filterActiveOnly);
}