package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyAppointmentV2View {

    private static final String REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";

    @JsonProperty("service_address")
    private AddressAPI serviceAddress;

    @JsonProperty("usual_residential_address")
    private AddressAPI usualResidentialAddress;

    @JsonProperty("appointed_on")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Instant appointedOn;

    @JsonProperty("resigned_on")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Instant resignedOn;

    @JsonProperty("country_of_residence")
    private String countryOfResidence;

    @JsonProperty("date_of_birth")
    private DateOfBirth dateOfBirth;

    @JsonProperty("former_names")
    private List<FormerNamesAPI> formerNames;

    @JsonProperty("identification")
    private IdentificationAPI identification;

    @JsonProperty("links")
    private LinksAPI links;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("officer_role")
    private String officerRole;

    public AddressAPI getServiceAddress() {
        return serviceAddress;
    }

    public AddressAPI getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public Instant getAppointedOn() {
        return appointedOn;
    }

    public Instant getResignedOn() {
        return resignedOn;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public DateOfBirth getDateOfBirth() {
        return dateOfBirth;
    }

    public List<FormerNamesAPI> getFormerNames() {
        return formerNames;
    }

    public IdentificationAPI getIdentification() {
        return identification;
    }

    public LinksAPI getLinks() {
        return links;
    }

    public String getName() {
        return name;
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

    public static class CompanyAppointmentV2ViewBuilder {

        public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
        private List<Consumer<CompanyAppointmentV2View>> buildSteps;

        private CompanyAppointmentV2ViewBuilder() {
            buildSteps = new ArrayList<>();
        }

        public static CompanyAppointmentV2ViewBuilder view(OfficerAPI data) {

            CompanyAppointmentV2ViewBuilder builder = new CompanyAppointmentV2ViewBuilder();

            return builder.withServiceAddress(data.getServiceAddress())
                .withUsualResidentialAddress(data.getUsualResidentialAddress())
                .withAppointedOn(data.getAppointedOn())
                .withCountryOfResidence(data.getCountryOfResidence())
                .withDateOfBirth(builder.mapDateOfBirth(data.getDateOfBirth()))
                .withFormerNames(data.getFormerNameData())
                .withIdentification(data.getIdentificationData())
                .withLinks(data.getLinksData())
                .withName(builder.individualOfficerName(data))
                .withNationality(data.getNationality())
                .withOccupation(data.getOccupation())
                .withOfficerRole(data.getOfficerRole())
                .withResignedOn(data.getResignedOn());
        }

        public CompanyAppointmentV2ViewBuilder withServiceAddress(AddressAPI address) {

            buildSteps.add(view -> view.serviceAddress = address);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withUsualResidentialAddress(AddressAPI address) {

            buildSteps.add(view -> view.usualResidentialAddress = address);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withAppointedOn(Instant appointedOn) {

            buildSteps.add(view -> view.appointedOn = appointedOn);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withCountryOfResidence(String countryOfResidence) {

            buildSteps.add(view -> view.countryOfResidence = countryOfResidence);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withDateOfBirth(DateOfBirth dateOfBirth) {

            buildSteps.add(view -> view.dateOfBirth = dateOfBirth);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withFormerNames(List<FormerNamesAPI> formerNames) {

            buildSteps.add(view -> view.formerNames = formerNames);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withIdentification(IdentificationAPI identification) {

            buildSteps.add(view -> view.identification = identification);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withLinks(LinksAPI links) {

            if (links != null && links.getOfficerLinksData() != null) {
                links.getOfficerLinksData().setSelfLink(null);
            }

            buildSteps.add(view -> view.links = links);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withName(String name) {

            buildSteps.add(view -> view.name = name);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withNationality(String nationality) {

            buildSteps.add(view -> view.nationality = nationality);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withOccupation(String occupation) {

            buildSteps.add(view -> view.occupation = occupation);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withOfficerRole(String officerRole) {

            buildSteps.add(view -> view.officerRole = officerRole);

            return this;
        }

        public CompanyAppointmentV2ViewBuilder withResignedOn(Instant resignedOn) {

            buildSteps.add(view -> view.resignedOn = resignedOn);

            return this;
        }

        public CompanyAppointmentV2View build() {

            CompanyAppointmentV2View view = new CompanyAppointmentV2View();
            buildSteps.forEach(step -> step.accept(view));

            return view;
        }

        private String individualOfficerName(OfficerAPI officerData) {
            String result = officerData.getSurname();
            if (officerData.getForename() != null || officerData.getOtherForenames() != null) {
                result = String.join(", ", officerData.getSurname(), Stream.of(officerData.getForename(), officerData.getOtherForenames())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")));
            }
            if (officerData.getTitle() != null && !officerData.getTitle().matches(REGEX)) {
                result = String.join(", ", result, officerData.getTitle());
            }
            return result;
        }

        private DateOfBirth mapDateOfBirth(Instant dateOfBirth) {

            return Optional.ofNullable(dateOfBirth).map(dob -> dob.atZone(UTC_ZONE))
                .map(utc -> new DateOfBirth(
                    utc.get(ChronoField.DAY_OF_MONTH),
                    utc.get(ChronoField.MONTH_OF_YEAR),
                    utc.get(ChronoField.YEAR)))
                .orElse(null);
        }
    }
}
