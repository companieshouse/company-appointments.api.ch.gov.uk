package uk.gov.companieshouse.company_appointments.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.LinksAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;

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
public class CompanyAppointmentFullRecordView {

    private static final String TITLE_REGEX = "^(?i)(?=m)(?:mrs?|miss|ms|master)$";

    @JsonProperty("service_address")
    private AddressAPI serviceAddress;

    @JsonProperty("usual_residential_address")
    private AddressAPI usualResidentialAddress;

    @JsonProperty("appointed_on")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Instant appointedOn;

    @JsonProperty("appointed_before")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Instant appointedBefore;

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

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("person_number")
    private String personNumber;

    public AddressAPI getServiceAddress() {
        return serviceAddress;
    }

    public AddressAPI getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public Instant getAppointedOn() {
        return appointedOn;
    }

    public Instant getAppointedBefore() {
        return appointedBefore;
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

    public String getEtag() {
        return etag;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public static class Builder {

        public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
        private static final String FULL_RECORD = "/full_record";
        private final List<Consumer<CompanyAppointmentFullRecordView>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public static Builder view(OfficerAPI data, SensitiveOfficerAPI sensitiveData) {

            Builder builder = new Builder();

            return builder.withServiceAddress(data.getServiceAddress())
                .withUsualResidentialAddress(sensitiveData.getUsualResidentialAddress())
                .withAppointedOn(data.getAppointedOn())
                .withAppointedBefore(data.getAppointedBefore())
                .withCountryOfResidence(data.getCountryOfResidence())
                .withDateOfBirth(builder.mapDateOfBirth(sensitiveData.getDateOfBirth()))
                .withFormerNames(data.getFormerNameData())
                .withIdentification(data.getIdentificationData())
                .withLinks(data.getLinksData())
                .withName(builder.individualOfficerName(data))
                .withNationality(data.getNationality())
                .withOccupation(data.getOccupation())
                .withOfficerRole(data.getOfficerRole())
                .withResignedOn(data.getResignedOn())
                .withEtag(data.getEtag())
                .withPersonNumber(data.getPersonNumber());
        }

        private static void appendSelfLinkFullRecord(CompanyAppointmentFullRecordView view) {
            if (view != null && view.getLinks() != null) {
                final String selfLink = view.getLinks().getSelfLink();

                if (selfLink != null && !selfLink.endsWith(FULL_RECORD)) {
                    view.getLinks().setSelfLink(selfLink.concat(FULL_RECORD));
                }
            }
        }

        public Builder withServiceAddress(AddressAPI address) {

            buildSteps.add(view -> view.serviceAddress = address);

            return this;
        }

        public Builder withUsualResidentialAddress(AddressAPI address) {

            buildSteps.add(view -> view.usualResidentialAddress = address);

            return this;
        }

        public Builder withAppointedOn(Instant appointedOn) {

            buildSteps.add(view -> view.appointedOn = appointedOn);

            return this;
        }

        public Builder withAppointedBefore(Instant appointedBefore) {

            buildSteps.add(view -> view.appointedBefore = appointedBefore);

            return this;
        }

        public Builder withCountryOfResidence(String countryOfResidence) {

            buildSteps.add(view -> view.countryOfResidence = countryOfResidence);

            return this;
        }

        public Builder withDateOfBirth(DateOfBirth dateOfBirth) {

            buildSteps.add(view -> view.dateOfBirth = dateOfBirth);

            return this;
        }

        public Builder withFormerNames(List<FormerNamesAPI> formerNames) {

            buildSteps.add(view -> view.formerNames = formerNames);

            return this;
        }

        public Builder withIdentification(IdentificationAPI identification) {

            buildSteps.add(view -> view.identification = identification);

            return this;
        }

        public Builder withLinks(LinksAPI links) {

            if (links != null && links.getOfficerLinksData() != null) {
                links.getOfficerLinksData().setSelfLink(null);
            }

            buildSteps.add(view -> view.links = links);
            buildSteps.add(Builder::appendSelfLinkFullRecord);

            return this;
        }

        public Builder withName(String name) {

            buildSteps.add(view -> view.name = name);

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

        public Builder withOfficerRole(String officerRole) {

            buildSteps.add(view -> view.officerRole = officerRole);

            return this;
        }

        public Builder withResignedOn(Instant resignedOn) {

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

        public CompanyAppointmentFullRecordView build() {

            CompanyAppointmentFullRecordView view = new CompanyAppointmentFullRecordView();
            buildSteps.forEach(step -> step.accept(view));

            return view;
        }

        private String individualOfficerName(OfficerAPI officerData) {
            if (officerData.getCompanyName() != null) {
                return officerData.getCompanyName();
            }

            String result = officerData.getSurname();
            if (officerData.getForename() != null || officerData.getOtherForenames() != null) {
                result = String.join(", ", officerData.getSurname(), Stream.of(officerData.getForename(), officerData.getOtherForenames())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" ")));
            }
            if (officerData.getTitle() != null && !officerData.getTitle().matches(TITLE_REGEX)) {
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
