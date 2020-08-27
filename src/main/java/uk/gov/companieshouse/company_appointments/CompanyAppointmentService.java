package uk.gov.companieshouse.company_appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;

@Service
public class CompanyAppointmentService {

    private CompanyAppointmentRepository companyAppointmentRepository;
    private CompanyAppointmentMapper companyAppointmentMapper;

    @Autowired
    public CompanyAppointmentService(CompanyAppointmentRepository companyAppointmentRepository,
                                     CompanyAppointmentMapper companyAppointmentMapper) {
        this.companyAppointmentRepository = companyAppointmentRepository;
        this.companyAppointmentMapper = companyAppointmentMapper;
    }

    public CompanyAppointmentView fetchAppointment(String companyNumber, String appointmentID) throws NotFoundException {
        return companyAppointmentMapper.map(companyAppointmentRepository.readByCompanyNumberAndAppointmentID(companyNumber, appointmentID)
                        .orElseThrow(() -> new NotFoundException(String.format("Appointment '%s' not found", appointmentID))));
    }

}
