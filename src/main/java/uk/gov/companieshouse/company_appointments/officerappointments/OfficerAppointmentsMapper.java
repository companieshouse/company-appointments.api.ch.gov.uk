package uk.gov.companieshouse.company_appointments.officerappointments;

import static java.util.Optional.ofNullable;
import static uk.gov.companieshouse.api.officer.AppointmentList.KindEnum;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.officer.OfficerLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.CompanyAppointmentDocument;

@Component
class OfficerAppointmentsMapper {

    private final ItemsMapper itemsMapper;
    private final NameMapper nameMapper;
    private final DateOfBirthMapper dobMapper;
    private final OfficerRoleMapper roleMapper;

    OfficerAppointmentsMapper(ItemsMapper itemsMapper,
            NameMapper nameMapper,
            DateOfBirthMapper dobMapper,
            OfficerRoleMapper roleMapper) {
        this.itemsMapper = itemsMapper;
        this.nameMapper = nameMapper;
        this.dobMapper = dobMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * Maps the appointments returned from MongoDB to a list of officer appointments alongside top
     * level fields, relating to the first appointment found.
     *
     * @param mapperRequest@return The optional OfficerAppointmentsApi for the response body.
     */
    Optional<AppointmentList> mapOfficerAppointments(MapperRequest mapperRequest) {
        return ofNullable(mapperRequest.getFirstAppointment())
                .flatMap(firstAppointment -> ofNullable(firstAppointment.getData())
                        .map(data -> {
                            OfficerAppointmentsAggregate aggregate = mapperRequest.getAggregate();
                            return new AppointmentList()
                                    .etag(data.getEtag())
                                    .isCorporateOfficer(
                                            roleMapper.mapIsCorporateOfficer(data.getOfficerRole()))
                                    .itemsPerPage(mapperRequest.getItemsPerPage())
                                    .kind(KindEnum.PERSONAL_APPOINTMENT)
                                    .links(new OfficerLinkTypes().self(
                                            String.format("/officers/%s/appointments",
                                                    mapperRequest.getFirstAppointment()
                                                            .getOfficerId())))
                                    .items(itemsMapper.map(aggregate.getOfficerAppointments()))
                                    .name(nameMapper.map(data))
                                    .startIndex(mapperRequest.getStartIndex())
                                    .totalResults(aggregate.getTotalResults())
                                    .activeCount(aggregate.getTotalResults()
                                            - aggregate.getInactiveCount()
                                            - aggregate.getResignedCount())
                                    .inactiveCount(aggregate.getInactiveCount())
                                    .resignedCount(aggregate.getResignedCount());
                        })
                        .map(appointmentList -> appointmentList.dateOfBirth(
                                ofNullable(firstAppointment.getSensitiveData())
                                        .map(sensitiveData -> dobMapper.map(
                                                sensitiveData.getDateOfBirth(),
                                                firstAppointment.getData().getOfficerRole()))
                                        .orElse(null))));
    }

    static class MapperRequest {

        private Integer startIndex;
        private Integer itemsPerPage;
        private CompanyAppointmentDocument firstAppointment;
        private OfficerAppointmentsAggregate aggregate;

        Integer getStartIndex() {
            return startIndex;
        }

        MapperRequest startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        Integer getItemsPerPage() {
            return itemsPerPage;
        }

        MapperRequest itemsPerPage(Integer itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        CompanyAppointmentDocument getFirstAppointment() {
            return firstAppointment;
        }

        MapperRequest firstAppointment(CompanyAppointmentDocument firstAppointment) {
            this.firstAppointment = firstAppointment;
            return this;
        }

        OfficerAppointmentsAggregate getAggregate() {
            return aggregate;
        }

        MapperRequest aggregate(OfficerAppointmentsAggregate aggregate) {
            this.aggregate = aggregate;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MapperRequest that = (MapperRequest) o;
            return Objects.equals(startIndex, that.startIndex) && Objects.equals(itemsPerPage,
                    that.itemsPerPage) && Objects.equals(firstAppointment, that.firstAppointment)
                    && Objects.equals(aggregate, that.aggregate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startIndex, itemsPerPage, firstAppointment, aggregate);
        }
    }
}
