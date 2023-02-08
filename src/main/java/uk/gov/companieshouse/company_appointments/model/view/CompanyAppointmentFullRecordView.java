package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.api.appointment.ContactDetails;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.api.model.delta.officers.DeltaAppointmentApi;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyAppointmentFullRecordView {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";

    @JsonProperty("service_address")
    private ServiceAddress serviceAddress;

    @JsonProperty("usual_residential_address")
    private UsualResidentialAddress usualResidentialAddress;

    @JsonProperty("appointed_on")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDate appointedOn;

    @JsonProperty("appointed_before")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDate appointedBefore;

    @JsonProperty("resigned_on")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDate resignedOn;

    @JsonProperty("country_of_residence")
    private String countryOfResidence;

    @JsonProperty("date_of_birth")
    private DateOfBirthView dateOfBirth;

    @JsonProperty("former_names")
    private List<FormerNames> formerNames;

    @JsonProperty("identification")
    private Identification identification;

    @JsonProperty("links")
    private ItemLinkTypes links;

    @JsonProperty("name")
    private String name;

    @JsonProperty("forename")
    private String forename;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("other_forenames")
    private String otherForenames;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("officer_role")
    private String officerRole;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("person_number")
    private String personNumber;

    @JsonProperty("is_pre_1992_appointment")
    private Boolean isPre1992Appointment;

    @JsonProperty("contact_details")
    private ContactDetails contactDetails;

    @JsonProperty("principal_office_address")
    private PrincipalOfficeAddress principalOfficeAddress;

    @JsonProperty("responsibilities")
    private String responsibilities;

    public ServiceAddress getServiceAddress() {
        return serviceAddress;
    }

    public UsualResidentialAddress getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public LocalDate getAppointedOn() {
        return appointedOn;
    }

    public LocalDate getAppointedBefore() {
        return appointedBefore;
    }

    public LocalDate getResignedOn() {
        return resignedOn;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public DateOfBirthView getDateOfBirth() {
        return dateOfBirth;
    }

    public List<FormerNames> getFormerNames() {
        return formerNames;
    }

    public Identification getIdentification() {
        return identification;
    }

    public ItemLinkTypes getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getOtherForenames() {
        return otherForenames;
    }

    public String getNationality() {
        return nationality;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getOfficerRole() {
        return officerRole;
    }

    public String getEtag() {
        return etag;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public Boolean getIsPre1992Appointment() {
        return isPre1992Appointment;
    }

    public ContactDetails getContactDetails() {return contactDetails;}

    public String getResponsibilities() {return responsibilities;}

    public PrincipalOfficeAddress getPrincipalOfficeAddress() {return principalOfficeAddress;}

    public static class Builder {

        public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
        private static final String FULL_RECORD = "/full_record";
        private final List<Consumer<CompanyAppointmentFullRecordView>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public static Builder view(DeltaAppointmentApi api) {

            Builder builder = new Builder();

            return builder.withServiceAddress(api.getData().getServiceAddress())
                    .withUsualResidentialAddress(api.getSensitiveData().getUsualResidentialAddress())
                    .withAppointedOn(api.getData().getAppointedOn())
                    .withAppointedBefore(api.getData().getAppointedBefore())
                    .withCountryOfResidence(api.getData().getCountryOfResidence())
                    .withDateOfBirth(builder.mapDateOfBirth(api.getSensitiveData()))
                    .withFormerNames(api.getData().getFormerNames())
                    .withIdentification(api.getData().getIdentification())
                    .withLinks(api.getData().getLinks())
                    .withName(builder.individualOfficerName(api))
                    .withForename(api.getData().getForename())
                    .withSurname(api.getData().getSurname())
                    .withOtherForenames(api.getData().getOtherForenames())
                    .withNationality(api.getData().getNationality())
                    .withOccupation(api.getData().getOccupation())
                    .withOfficerRole(api.getData().getOfficerRole())
                    .withResignedOn(api.getData().getResignedOn())
                    .withEtag(api.getEtag())
                    .withPersonNumber(api.getData().getPersonNumber())
                    .withIsPre1992Appointment(api.getData().getIsPre1992Appointment())
                    .withContactDetails(api.getData().getContactDetails())
                    .withResponsibilities(api.getData().getResponsibilities())
                    .withPrincipleOfficeAddress(api.getData().getPrincipalOfficeAddress());
        }

        private static void appendSelfLinkFullRecord(CompanyAppointmentFullRecordView view) {
            if (view != null && view.getLinks() != null) {
                final String selfLink = view.getLinks().getSelf();

                if (!selfLink.endsWith(FULL_RECORD)) {
                    view.getLinks().setSelf(selfLink.concat(FULL_RECORD));
                }
            }
        }

        public Builder withServiceAddress(ServiceAddress address) {

            buildSteps.add(view -> view.serviceAddress = address);

            return this;
        }

        public Builder withUsualResidentialAddress(UsualResidentialAddress address) {

            buildSteps.add(view -> view.usualResidentialAddress = address);

            return this;
        }

        public Builder withAppointedOn(LocalDate appointedOn) {

            buildSteps.add(view -> view.appointedOn = appointedOn);

            return this;
        }

        public Builder withAppointedBefore(LocalDate appointedBefore) {

            buildSteps.add(view -> view.appointedBefore = appointedBefore);

            return this;
        }

        public Builder withCountryOfResidence(String countryOfResidence) {

            buildSteps.add(view -> view.countryOfResidence = countryOfResidence);

            return this;
        }

        public Builder withDateOfBirth(DateOfBirthView dateOfBirth) {

            buildSteps.add(view -> view.dateOfBirth = dateOfBirth);

            return this;
        }

        public Builder withFormerNames(List<FormerNames> formerNames) {

            buildSteps.add(view -> view.formerNames = formerNames);

            return this;
        }

        public Builder withIdentification(Identification identification) {

            buildSteps.add(view -> view.identification = identification);

            return this;
        }

        public Builder withLinks(List<ItemLinkTypes> links) {

            if (links != null && links.get(0) != null) {
                links.get(0).getOfficer().setSelf(null);
            }

            buildSteps.add(view -> view.links = links.get(0));
            buildSteps.add(Builder::appendSelfLinkFullRecord);

            return this;
        }

        public Builder withName(String name) {

            buildSteps.add(view -> view.name = name);

            return this;
        }

        public Builder withForename(String forename) {

            buildSteps.add(view -> view.forename = forename);

            return this;
        }

        public Builder withSurname(String surname) {

            buildSteps.add(view -> view.surname = surname);

            return this;
        }

        public Builder withOtherForenames(String otherForenames) {

            buildSteps.add(view -> view.otherForenames = otherForenames);

            return this;
        }

        public Builder withNationality(String nationality) {

            buildSteps.add(view -> view.nationality = nationality);

            return this;
        }

        public Builder withOccupation(String occupation) {

            buildSteps.add(view -> view.occupation = occupation);

            return this;
        }

        public Builder withOfficerRole(Data.OfficerRoleEnum officerRole) {

            String officerRoleToString = officerRole.toString();

            buildSteps.add(view -> view.officerRole = officerRoleToString);

            return this;
        }

        public Builder withResignedOn(LocalDate resignedOn) {

            buildSteps.add(view -> view.resignedOn = resignedOn);

            return this;
        }

        public Builder withEtag(String etag) {

            buildSteps.add(view -> view.etag = etag);

            return this;
        }

        public Builder withPersonNumber(String personNumber) {

            buildSteps.add(view -> view.personNumber = personNumber);

            return this;
        }

        public Builder withIsPre1992Appointment(Boolean isPre1992Appointment) {

            buildSteps.add(view -> view.isPre1992Appointment = isPre1992Appointment);

            return this;
        }

        public Builder withContactDetails(ContactDetails contactDetails) {

            buildSteps.add(view -> view.contactDetails = contactDetails);

            return this;
        }

        public Builder withResponsibilities(String responsibilities) {

            buildSteps.add(view -> view.responsibilities = responsibilities);

            return this;
        }

        public Builder withPrincipleOfficeAddress(PrincipalOfficeAddress principalOfficeAddress) {

            buildSteps.add(view -> view.principalOfficeAddress = principalOfficeAddress);

            return this;
        }

        public CompanyAppointmentFullRecordView build() {

            CompanyAppointmentFullRecordView view = new CompanyAppointmentFullRecordView();
            buildSteps.forEach(step -> step.accept(view));

            return view;
        }

        private String individualOfficerName(DeltaAppointmentApi api) {
            if (api.getData().getCompanyName() != null) {
                return api.getData().getCompanyName();
            }

            String result = api.getData().getSurname();
            if (api.getData().getForename() != null || api.getData().getOtherForenames() != null) {
                result = String.join(", ", api.getData().getSurname(), Stream.of(api.getData().getForename(), api.getData().getOtherForenames())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")));
            }
            if (api.getData().getTitle() != null && !api.getData().getTitle().matches(TITLE_REGEX)) {
                result = String.join(", ", result, api.getData().getTitle());
            }

            return result;
        }

        private DateOfBirthView mapDateOfBirth(SensitiveData sensitiveData) {
            return Optional.ofNullable(sensitiveData.getDateOfBirth()).
                    map(dob -> new DateOfBirthView(
                            dob.getDay(),
                            dob.getMonth(),
                            dob.getYear()))
                    .orElse(null);
        }
    }
}
