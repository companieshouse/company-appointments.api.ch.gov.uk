package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.api.officer.AppointmentList.KindEnum;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Component
class OfficerAppointmentsMapper {

    private final OfficerRoleMapper roleMapper;
    private final ItemsMapper itemsMapper;
    private final NameMapper nameMapper;
    private final DateOfBirthMapper dobMapper;

    OfficerAppointmentsMapper(OfficerRoleMapper roleMapper, ItemsMapper itemsMapper, NameMapper nameMapper,
            DateOfBirthMapper dobMapper) {
        this.roleMapper = roleMapper;
        this.itemsMapper = itemsMapper;
        this.nameMapper = nameMapper;
        this.dobMapper = dobMapper;
    }


    /**
     * Maps the appointments returned from MongoDB to a list of officer appointments alongside top level fields,
     * relating to the first appointment found.
     *
     * @param mapperRequest@return The optional AppointmentList for the response body.
     */
    Optional<AppointmentList> mapOfficerAppointments(MapperRequest mapperRequest) {
        return ofNullable(mapperRequest.firstAppointment())
                .flatMap(firstAppointment -> ofNullable(firstAppointment.getData())
                        .map(data -> new AppointmentList()
                                .etag(data.getEtag())
                                .isCorporateOfficer(roleMapper.mapIsCorporateOfficer(data.getOfficerRole()))
                                .itemsPerPage(mapperRequest.itemsPerPage())
                                .kind(KindEnum.PERSONAL_APPOINTMENT)
                                .links(new OfficerLinkTypes()
                                        .self("/officers/%s/appointments".formatted(firstAppointment.getOfficerId())))
                                .items(itemsMapper.map(mapperRequest.officerAppointments()))
                                .name(nameMapper.map(data))
                                .startIndex(mapperRequest.startIndex())
                                .totalResults(mapperRequest.totalResults())
                                .activeCount(mapperRequest.totalResults() - mapperRequest.inactiveCount()
                                        - mapperRequest.resignedCount())
                                .inactiveCount(mapperRequest.inactiveCount())
                                .resignedCount(mapperRequest.resignedCount()))
                        .map(appointmentList -> appointmentList.dateOfBirth(
                                ofNullable(firstAppointment.getSensitiveData())
                                        .map(sensitiveData -> dobMapper.map(
                                                sensitiveData.getDateOfBirth(),
                                                firstAppointment.getData().getOfficerRole()))
                                        .orElse(null))));
    }

    record MapperRequest(Integer startIndex, Integer itemsPerPage, CompanyAppointmentDocument firstAppointment,
                         List<CompanyAppointmentDocument> officerAppointments, int totalResults,
                         int inactiveCount, int resignedCount) {

        private MapperRequest(Builder builder) {
            this(builder.startIndex, builder.itemsPerPage, builder.firstAppointment, builder.officerAppointments,
                    builder.totalResults, builder.inactiveCount, builder.resignedCount);
        }

        static Builder builder() {
            return new Builder();
        }

        static final class Builder {

            private Integer startIndex;
            private Integer itemsPerPage;
            private CompanyAppointmentDocument firstAppointment;
            private List<CompanyAppointmentDocument> officerAppointments;
            private int totalResults;
            private int inactiveCount;
            private int resignedCount;

            private Builder() {
            }

            Builder startIndex(Integer startIndex) {
                this.startIndex = startIndex;
                return this;
            }

            Builder itemsPerPage(Integer itemsPerPage) {
                this.itemsPerPage = itemsPerPage;
                return this;
            }

            Builder firstAppointment(CompanyAppointmentDocument firstAppointment) {
                this.firstAppointment = firstAppointment;
                return this;
            }

            Builder officerAppointments(List<CompanyAppointmentDocument> officerAppointments) {
                this.officerAppointments = officerAppointments;
                return this;
            }

            Builder totalResults(int totalResults) {
                this.totalResults = totalResults;
                return this;
            }

            Builder inactiveCount(int inactiveCount) {
                this.inactiveCount = inactiveCount;
                return this;
            }

            Builder resignedCount(int resignedCount) {
                this.resignedCount = resignedCount;
                return this;
            }

            MapperRequest build() {
                return new MapperRequest(this);
            }
        }
    }
}
