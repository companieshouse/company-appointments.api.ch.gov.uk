package uk.gov.companieshouse.company_appointments;

public class CompanyAppointmentService {

    private CompanyAppointmentRepository companyAppointmentRepository;
    private CompanyAppointmentMapper companyAppointmentMapper;

    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
            CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public CompanyAppointment fetchAppointment(String companyNumber, String appointmentID) {
        return companyAppointmentMapper
                .map(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID));
    }

}
