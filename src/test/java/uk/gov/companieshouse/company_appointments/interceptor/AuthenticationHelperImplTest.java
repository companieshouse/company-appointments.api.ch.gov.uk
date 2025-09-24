package uk.gov.companieshouse.company_appointments.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperImplTest {
    private static final String USER_EMAIL = "user@somewhere.email.com";
    private static final String USER_FORENAME = "Quentin";
    private static final String USER_SURNAME = "Schaden";

    private AuthenticationHelper testHelper;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        testHelper = new AuthenticationHelperImpl();
    }

    @Test
    void getAuthorisedIdentityWhenRequestNull() {

        assertThat(testHelper.getAuthorisedIdentity(null), is(nullValue()));
    }

    @Test
    void getAuthorisedIdentityWhenRequestNotNull() {
        String expected = "identity";

        when(request.getHeader("ERIC-Identity")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedIdentity(request), is(expected));
    }

    @Test
    void getAuthorisedIdentityType() {
        String expected = "identity-type";

        when(request.getHeader("ERIC-Identity-Type")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedIdentityType(request), is(expected));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIs() {
        assertThat(testHelper.isApiKeyIdentityType("key"), is(true));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIsNot() {
        assertThat(testHelper.isApiKeyIdentityType("KEY"), is(false));
    }

    @Test
    void isOauth2IdentityTypeWhenItIs() {
        assertThat(testHelper.isOauth2IdentityType("oauth2"), is(true));
    }

    @Test
    void isOauth2IdentityTypeWhenItIsNot() {
        assertThat(testHelper.isOauth2IdentityType("Oauth2"), is(false));
    }

    @Test
    void getAuthorisedUser() {
        String expected = "authorised-user";

        when(request.getHeader("ERIC-Authorised-User")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedUser(request), is(expected));
    }

    @Test
    void getAuthorisedUserEmail() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0};forename={1};surname={2}", USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserEmail(request), is(USER_EMAIL));
    }

    @Test
    void getAuthorisedUserEmailWhenUserNul() {
        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserEmailWhenUserMissing() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn("");

        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserEmailWhenEmpty() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(";");

        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserEmailWhenNull() {
        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForename() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0};forename={1};surname={2}", USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserForename(request), is(USER_FORENAME));
    }

    @Test
    void getAuthorisedUserForenameWhenUserNull() {
        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForenameWhenUserEmpty() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn("");

        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForenameWhenMissing() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0}", USER_EMAIL));

        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForenameWhenUnnamed() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0};{1}", USER_EMAIL, USER_FORENAME));

        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserSurname() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0};forename={1};surname={2}", USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserSurname(request), is(USER_SURNAME));
    }

    @Test
    void getAuthorisedUserSurnameWhenMissing() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0};forename={1}", USER_EMAIL, USER_FORENAME));

        assertThat(testHelper.getAuthorisedUserSurname(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserSurnameWhenUnnamed() {
        when(request.getHeader("ERIC-Authorised-User")).thenReturn(
                MessageFormat.format("{0};forename={1};{2}", USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserSurname(request), is(nullValue()));
    }

    @Test
    void getAuthorisedScope() {
        String expected = "authorised-scope";

        when(request.getHeader("ERIC-Authorised-Scope")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedScope(request), is(expected));
    }

    @Test
    void getAuthorisedRoles() {
        String expected = "authorised-roles";

        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedRoles(request), is(expected));
    }

    @Test
    void getAuthorisedRolesArray() {
        String[] expected = new String[]{"role-1", "role-2"};

        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("role-1 role-2");

        assertThat(testHelper.getAuthorisedRolesArray(request), is(expected));
    }

    @Test
    void getAuthorisedRolesArrayWhenRolesNull() {
        String[] expected = new String[]{};

        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn(null);

        assertThat(testHelper.getAuthorisedRolesArray(request), is(expected));
    }

    @Test
    void getAuthorisedRolesArrayWhenRolesEmpty() {
        String[] expected = new String[]{};

        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("");

        assertThat(testHelper.getAuthorisedRolesArray(request), is(expected));
    }

    @Test
    void isRoleAuthorisedWhenItIs() {
        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("role-1 role-2");

        assertThat(testHelper.isRoleAuthorised(request, "role-1"), is(true));
    }

    @Test
    void isRoleAuthorisedWhenItIsNot() {
        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("role-1 role-2");

        assertThat(testHelper.isRoleAuthorised(request, "role-0"), is(false));
    }

    @Test
    void isRoleAuthorisedWhenItIsNull() {
        assertThat(testHelper.isRoleAuthorised(request, null), is(false));
    }

    @Test
    void isRoleAuthorisedWhenItIsEmpty() {
        assertThat(testHelper.isRoleAuthorised(request, ""), is(false));
    }

    @Test
    void isRoleAuthorisedWhenRolesNull() {
        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn(null);

        assertThat(testHelper.isRoleAuthorised(request, "role-1"), is(false));
    }

    @Test
    void isRoleAuthorisedWhenRolesEmpty() {
        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("");

        assertThat(testHelper.isRoleAuthorised(request, "role-1"), is(false));
    }

    @Test
    void getKeyPrivileges() {
        Map<String, String[]> testValues = new HashMap<>();
        testValues.put("role-1", new String[]{"role-1"});
        testValues.put("role-1,role-2", new String[]{"role-1", "role-2"});

        testValues.forEach((headerValue, expectedPrivileges) -> {
            when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn(headerValue);

            assertThat(testHelper.getApiKeyPrivileges(request), is(expectedPrivileges));
        });
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsPUT() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges"))
                .thenReturn("other-role,internal-app");
        when(request.getMethod())
                .thenReturn("PUT");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(true));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsNotPUT() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("role-1,sensitive-data");
        when(request.getMethod())
                .thenReturn("PUT");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(false));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsGET() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges"))
                .thenReturn("other-role,sensitive-data");
        when(request.getMethod())
                .thenReturn("GET");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(true));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsNotGET() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("role-1,internal-app");
        when(request.getMethod())
                .thenReturn("GET");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(false));
    }

    @Test
    void getTokenPrivileges() {
        String tokenValue = "company_number=00006400 company_officers=readprotected,write";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        Map<String, List<String>> result = testHelper.getTokenPermissions(request);

        assertThat(result.containsKey("company_number"), is(true));
        assertThat(result.containsKey("company_officers"), is(true));
        assertThat(result.get("company_number").getFirst(), is("00006400"));
        assertThat(result.get("company_officers").getFirst(), is("readprotected"));
        assertThat(result.get("company_officers").get(1), is("write"));
    }

    @Test
    void isTokenProtectedAndCompanyAuthorisedIsTrue() {
        String tokenValue = "company_number=00006400 company_officers=readprotected,write";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        assertThat(testHelper.isTokenProtectedAndCompanyAuthorised(request, "00006400"), is(true));
    }

    @Test
    void isTokenProtectedAndCompanyAuthorisedIsFalseWhenNotProtected() {
        String tokenValue = "company_number=00006400 company_officers=read,write";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        assertThat(testHelper.isTokenProtectedAndCompanyAuthorised(request, "00006400"), is(false));
    }

    @Test
    void isTokenProtectedAndCompanyAuthorisedIsFalseWhenIncorrectCompany() {
        String tokenValue = "company_number=00006400 company_officers=readprotected,write";
        when(request.getHeader("ERIC-Authorised-Token-Permissions")).thenReturn(tokenValue);

        assertThat(testHelper.isTokenProtectedAndCompanyAuthorised(request, "00006401"), is(false));
    }

}
