package uk.gov.companieshouse.company_appointments.model.transformer;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.*;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;
import uk.gov.companieshouse.company_appointments.model.data.DeltaContactDetails;
import uk.gov.companieshouse.company_appointments.model.data.DeltaFormerNames;
import uk.gov.companieshouse.company_appointments.model.data.DeltaIdentification;
import uk.gov.companieshouse.company_appointments.model.data.DeltaItemLinkTypes;
import uk.gov.companieshouse.company_appointments.model.data.DeltaOfficerData;
import uk.gov.companieshouse.company_appointments.model.data.DeltaPrincipalOfficeAddress;
import uk.gov.companieshouse.company_appointments.model.data.DeltaServiceAddress;

@ExtendWith(MockitoExtension.class)
class DeltaOfficerDataTransformerTest {

    private static final LocalDate LOCAL_DATE = LocalDate.now().minusDays(1);

    @Mock
    private DeltaIdentificationTransformer identificationTransformer;
    @Mock
    private DeltaIdentityVerificationDetailsTransformer identityVerificationDetailsTransformer;
    @Mock
    private DeltaItemLinkTypesTransformer itemLinkTypesTransformer;
    @Mock
    private DeltaPrincipalOfficeAddressTransformer principalOfficeAddressTransformer;
    @Mock
    private DeltaServiceAddressTransformer serviceAddressTransformer;
    @InjectMocks
    private DeltaOfficerDataTransformer transformer;

    @Mock
    private ServiceAddress serviceAddress;
    @Mock
    private ItemLinkTypes itemLinkTypes;
    @Mock
    private Identification identification;
    @Mock
    private PrincipalOfficeAddress principalOfficeAddress;

    @Mock
    private DeltaServiceAddress deltaServiceAddress;
    @Mock
    private DeltaItemLinkTypes deltaItemLinkTypes;
    @Mock
    private DeltaIdentification deltaIdentification;
    @Mock
    private DeltaPrincipalOfficeAddress deltaPrincipalOfficeAddress;

    @Test
    void shouldTransformData() throws FailedToTransformException {
        // given
        when(identificationTransformer.transform(any(Identification.class))).thenReturn(deltaIdentification);
        when(itemLinkTypesTransformer.transform(any(ItemLinkTypes.class))).thenReturn(deltaItemLinkTypes);
        when(principalOfficeAddressTransformer.transform(any(PrincipalOfficeAddress.class))).thenReturn(deltaPrincipalOfficeAddress);
        when(serviceAddressTransformer.transform(any(ServiceAddress.class))).thenReturn(deltaServiceAddress);

        DeltaOfficerData expected = buildExpected();

        // when
        DeltaOfficerData actual = transformer.transform(buildSource());

        // then
        assertThat(actual.getEtag()).isNotNull();
        expected.setEtag(actual.getEtag());

        assertThat(actual).isEqualTo(expected);
        verify(identificationTransformer).transform(identification);
        verify(itemLinkTypesTransformer).transform(itemLinkTypes);
        verify(principalOfficeAddressTransformer).transform(principalOfficeAddress);
        verify(serviceAddressTransformer).transform(serviceAddress);
    }

    @Test
    void shouldTransformDataWithNullsAndEmptyLists() throws FailedToTransformException {
        // given
        Data source = buildSource()
                .identification(null)
                .links(emptyList())
                .principalOfficeAddress(null)
                .serviceAddress(null)
                .contactDetails(null)
                .formerNames(emptyList());

        DeltaOfficerData expected = buildExpected()
                .setIdentification(null)
                .setLinks(null)
                .setPrincipalOfficeAddress(null)
                .setServiceAddress(null)
                .setContactDetails(null)
                .setFormerNames(emptyList());

        // when
        DeltaOfficerData actual = transformer.transform(source);

        // then
        assertThat(actual.getEtag()).isNotNull();
        expected.setEtag(actual.getEtag());

        assertThat(actual).isEqualTo(expected);
        verifyNoInteractions(identificationTransformer);
        verifyNoInteractions(itemLinkTypesTransformer);
        verifyNoInteractions(principalOfficeAddressTransformer);
        verifyNoInteractions(serviceAddressTransformer);
    }

    @Test
    void shouldTransformDataWithNullLinksAndFormerNames() throws FailedToTransformException {
        // given
        Data source = buildSource()
                .links(null)
                .formerNames(null);

        DeltaOfficerData expected = buildExpected()
                .setLinks(null)
                .setFormerNames(null);

        when(identificationTransformer.transform(any(Identification.class))).thenReturn(deltaIdentification);
        when(principalOfficeAddressTransformer.transform(any(PrincipalOfficeAddress.class))).thenReturn(deltaPrincipalOfficeAddress);
        when(serviceAddressTransformer.transform(any(ServiceAddress.class))).thenReturn(deltaServiceAddress);

        // when
        DeltaOfficerData actual = transformer.transform(source);

        // then
        assertThat(actual.getEtag()).isNotNull();
        expected.setEtag(actual.getEtag());

        assertThat(actual).isEqualTo(expected);
        verify(identificationTransformer).transform(identification);
        verifyNoInteractions(itemLinkTypesTransformer);
        verify(principalOfficeAddressTransformer).transform(principalOfficeAddress);
        verify(serviceAddressTransformer).transform(serviceAddress);
    }

    @Test
    void shouldRethrowTransformExceptionWhenCaught() throws FailedToTransformException {
        // given
        when(identificationTransformer.transform(any(Identification.class))).thenThrow(new FailedToTransformException("Failed"));

        // when
        Executable executable = () -> transformer.transform(buildSource());

        // then
        FailedToTransformException exception = assertThrows(FailedToTransformException.class, executable);
        assertThat(exception.getMessage()).isEqualTo("Failed to transform Data: Failed");
    }

    private Data buildSource() {
        return new Data()
                .personNumber("person number")
                .serviceAddress(serviceAddress)
                .serviceAddressIsSameAsRegisteredOfficeAddress(true)
                .countryOfResidence("UK")
                .appointedOn(LOCAL_DATE)
                .appointedBefore(LOCAL_DATE)
                .isPre1992Appointment(false)
                .links(singletonList(itemLinkTypes))
                .nationality("British")
                .occupation("occupation")
                .officerRole(Data.OfficerRoleEnum.DIRECTOR)
                .isSecureOfficer(false)
                .identification(identification)
                .companyName("company name")
                .surname("surname")
                .forename("forename")
                .honours("honours")
                .otherForenames("other forenames")
                .title("title")
                .companyNumber("12345678")
                .contactDetails(new ContactDetails()
                        .contactName("contact name"))
                .principalOfficeAddress(principalOfficeAddress)
                .resignedOn(LOCAL_DATE)
                .responsibilities("responsibilities")
                .formerNames(singletonList(new FormerNames()
                        .forenames("John Tester")
                        .surname("surname")));
    }

    private DeltaOfficerData buildExpected() {
        return new DeltaOfficerData()
                .setPersonNumber("person number")
                .setServiceAddress(deltaServiceAddress)
                .setServiceAddressIsSameAsRegisteredOfficeAddress(true)
                .setCountryOfResidence("UK")
                .setAppointedOn(Instant.from(LOCAL_DATE.atStartOfDay(UTC)))
                .setAppointedBefore(Instant.from(LOCAL_DATE.atStartOfDay(UTC)))
                .setPre1992Appointment(false)
                .setLinks(deltaItemLinkTypes)
                .setNationality("British")
                .setOccupation("occupation")
                .setOfficerRole("director")
                .setSecureOfficer(false)
                .setIdentification(deltaIdentification)
                .setCompanyName("company name")
                .setSurname("surname")
                .setForename("forename")
                .setHonours("honours")
                .setOtherForenames("other forenames")
                .setTitle("title")
                .setCompanyNumber("12345678")
                .setContactDetails(new DeltaContactDetails()
                        .setContactName("contact name"))
                .setPrincipalOfficeAddress(deltaPrincipalOfficeAddress)
                .setResignedOn(Instant.from(LOCAL_DATE.atStartOfDay(UTC)))
                .setResponsibilities("responsibilities")
                .setFormerNames(singletonList(new DeltaFormerNames()
                        .setForenames("John Tester")
                        .setSurname("surname")));
    }
}