package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointedTo;
import uk.gov.companieshouse.api.officer.AppointmentLinkTypes;
import uk.gov.companieshouse.api.officer.NameElements;
import uk.gov.companieshouse.api.officer.OfficerAppointmentSummary;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentData;

@Component
public class ItemsMapper {

    private final AddressMapper addressMapper;
    private final ContactDetailsMapper contactDetailsMapper;
    private final NameMapper nameMapper;
    private final LocalDateMapper localDateMapper;
    private final FormerNamesMapper formerNamesMapper;
    private final IdentificationMapper identificationMapper;
    private final OfficerRoleMapper roleMapper;

    public ItemsMapper(AddressMapper addressMapper,
            ContactDetailsMapper contactDetailsMapper,
            NameMapper nameMapper,
            LocalDateMapper localDateMapper,
            FormerNamesMapper formerNamesMapper,
            IdentificationMapper identificationMapper,
            OfficerRoleMapper roleMapper) {
        this.addressMapper = addressMapper;
        this.contactDetailsMapper = contactDetailsMapper;
        this.nameMapper = nameMapper;
        this.localDateMapper = localDateMapper;
        this.formerNamesMapper = formerNamesMapper;
        this.identificationMapper = identificationMapper;
        this.roleMapper = roleMapper;
    }

    protected List<OfficerAppointmentSummary> map(List<CompanyAppointmentData> appointments) {
        return appointments.stream()
                .map(appointment -> ofNullable(appointment.getData())
                        .map(data -> new OfficerAppointmentSummary()
                                .address(addressMapper.map(data.getServiceAddress()))
                                .appointedBefore(localDateMapper.map(data.getAppointedBefore()))
                                .appointedOn(localDateMapper.map(data.getAppointedOn()))
                                .appointedTo(new AppointedTo()
                                        .companyName(appointment.getCompanyName())
                                        .companyNumber(data.getCompanyNumber())
                                        .companyStatus(appointment.getCompanyStatus()))
                                .contactDetails(contactDetailsMapper.map(data.getContactDetails()))
                                .name(nameMapper.map(data))
                                .countryOfResidence(data.getCountryOfResidence())
                                .formerNames(formerNamesMapper.map(data.getFormerNameData()))
                                .identification(identificationMapper.map(data.getIdentificationData()))
                                .isPre1992Appointment(data.getIsPre1992Appointment())
                                .links(new AppointmentLinkTypes().company(String.format("/company/%s", data.getCompanyNumber())))
                                .nameElements(new NameElements()
                                        .forename(data.getForename())
                                        .title(data.getTitle())
                                        .otherForenames(data.getOtherForenames())
                                        .surname(data.getSurname())
                                        .honours(data.getHonours()))
                                .nationality(data.getNationality())
                                .occupation(data.getOccupation())
                                .officerRole(roleMapper.mapOfficerRole(data.getOfficerRole()))
                                .principalOfficeAddress(addressMapper.map(data.getPrincipalOfficeAddress()))
                                .resignedOn(localDateMapper.map(data.getResignedOn()))
                                .responsibilities(data.getResponsibilities()))
                        .orElse(null))
                .collect(Collectors.toList());
    }
}
