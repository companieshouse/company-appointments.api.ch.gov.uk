package uk.gov.companieshouse.company_appointments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CompanyAppointmentsApplication {

    public static final String APPLICATION_NAME_SPACE = "company-appointments.api.ch.gov.uk";

    public static void main(String[] args) {
        SpringApplication.run(CompanyAppointmentsApplication.class, args);
    }

}
