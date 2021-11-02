package uk.gov.companieshouse.company_appointments;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.company_appointments.config.LoggingConfig;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationHelperImpl;
import uk.gov.companieshouse.company_appointments.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentFullRecordView;
import uk.gov.companieshouse.company_appointments.model.view.CompanyAppointmentView;
import uk.gov.companieshouse.logging.Logger;

@WebMvcTest(controllers = {CompanyAppointmentController.class, CompanyAppointmentFullRecordController.class})
@Import({LoggingConfig.class, AuthenticationHelperImpl.class})
class AuthenticationInterceptorsIT {
    private static final String APP_ID = "N-YqKNwdT_HvetusfTJ0H0jAQbA";
    private static final String COMPANY_NUMBER = "09876543";
    private static final String AUTH_EMAIL = "user@somewhere.com";
    private static final String NAME = "OFFICER";
    public static final String URL_TEMPLATE = "/company/{company_number}/appointments/{appointment_id}";
    public static final String URL_TEMPLATE_FULL_RECORD = URL_TEMPLATE + "/full_record";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @MockBean
    private Logger logger;
    @MockBean
    private CompanyAppointmentService companyAppointmentService;
    @MockBean
    private CompanyAppointmentFullRecordService companyAppointmentFullRecordService;

    private HttpHeaders httpHeaders;
    private CompanyAppointmentView companyAppointmentView;
    private CompanyAppointmentFullRecordView companyAppointmentFullRecordView;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        companyAppointmentView = CompanyAppointmentView.builder().withName(NAME)
                .build();
        companyAppointmentFullRecordView =
                CompanyAppointmentFullRecordView.Builder.view(new OfficerAPI()).withName(NAME)
                        .build();
    }

    @Test
    void fetchAppointmentWhenOauth2AuthThenAllowed() throws Exception {
        addOauth2Headers();
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APP_ID)).thenReturn(companyAppointmentView);
        mockMvc.perform(get(URL_TEMPLATE, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    void fetchAppointmentWhenPrivilegedKeyAuthThenAllowed() throws Exception {
        addApiKeyHeaders(true);
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APP_ID)).thenReturn(companyAppointmentView);
        mockMvc.perform(get(URL_TEMPLATE, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    void fetchAppointmentWhenNonPrivilegedKeyAuthThenAllowed() throws Exception {
        addApiKeyHeaders(false);
        when(companyAppointmentService.fetchAppointment(COMPANY_NUMBER, APP_ID)).thenReturn(companyAppointmentView);
        mockMvc.perform(get(URL_TEMPLATE, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    void fetchAppointmentWhenAuthMissingThenDenied() throws Exception {
        mockMvc.perform(get(URL_TEMPLATE, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAppointmentWhenPrivilegedKeyAuthThenAllowed() throws Exception {
        addApiKeyHeaders(true);
        when(companyAppointmentFullRecordService.getAppointment(COMPANY_NUMBER, APP_ID)).thenReturn(
                companyAppointmentFullRecordView);
        mockMvc.perform(get(URL_TEMPLATE_FULL_RECORD, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    void getAppointmentWhenPrivilegedKeyMissingThenDenied() throws Exception {
        addOauth2Headers();
        mockMvc.perform(get(URL_TEMPLATE_FULL_RECORD, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAppointmentWhenAuthKeyNotPrivilegedThenDenied() throws Exception {
        addApiKeyHeaders(false);
        mockMvc.perform(get(URL_TEMPLATE_FULL_RECORD, COMPANY_NUMBER, APP_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private void addOauth2Headers() {
        httpHeaders.add("ERIC-Identity-Type", "oauth2");
        httpHeaders.add("ERIC-Identity", "user");
        httpHeaders.add("ERIC-Authorised-User", AUTH_EMAIL);
    }

    private void addApiKeyHeaders(final boolean isPrivileged) {
        httpHeaders.add("ERIC-Identity-Type", "key");
        httpHeaders.add("ERIC-Identity", "user");
        httpHeaders.add("ERIC-Authorised-User", AUTH_EMAIL);
        httpHeaders.add("ERIC-Authorised-Key-Roles", isPrivileged ? "*" : "none");
    }

}
