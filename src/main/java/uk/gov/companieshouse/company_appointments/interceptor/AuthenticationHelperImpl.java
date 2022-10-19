package uk.gov.companieshouse.company_appointments.interceptor;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class for user authentication
 */
@Component
public class AuthenticationHelperImpl implements AuthenticationHelper {
    public static final String OAUTH2_IDENTITY_TYPE = "oauth2";
    public static final String API_KEY_IDENTITY_TYPE = "key";

    public static final int USER_EMAIL_INDEX = 0;
    public static final int USER_FORENAME_INDEX = 1;
    public static final int USER_SURNAME_INDEX = 2;
    private static final String INTERNAL_APP_PRIVILEGE = "internal-app";
    private static final String SENSITIVE_DATA_PRIVILEGE = "sensitive-data";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER
            = "ERIC-Authorised-Key-Privileges";
    private static final String ERIC_AUTHORISED_TOKEN_PERMISSIONS_HEADER
            = "ERIC-Authorised-Token-Permissions";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";
    private static final String ERIC_AUTHORISED_SCOPE = "ERIC-Authorised-Scope";
    private static final String ERIC_AUTHORISED_ROLES = "ERIC-Authorised-Roles";
    private static final String COMPANY_OFFICER_PERMISSION = "company_officers";
    private static final String READ_PROTECTED = "read-protected";
    private static final String COMPANY_NUMBER_PERMISSION = "company_number";

    private static final String PUT_METHOD = "PUT";

    @Override
    public String getAuthorisedIdentity(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_IDENTITY);
    }

    @Override
    public String getAuthorisedIdentityType(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_IDENTITY_TYPE);
    }

    @Override
    public boolean isApiKeyIdentityType(final String identityType) {
        return API_KEY_IDENTITY_TYPE.equals(identityType);
    }

    @Override
    public boolean isOauth2IdentityType(final String identityType) {
        return OAUTH2_IDENTITY_TYPE.equals(identityType);
    }

    @Override
    public String getAuthorisedUser(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_AUTHORISED_USER);
    }

    @Override
    public String getAuthorisedUserEmail(HttpServletRequest request) {
        final String authorisedUser = getAuthorisedUser(request);

        if (authorisedUser == null || authorisedUser.trim().length() == 0) {
            return null;
        } else {
            final String[] details = authorisedUser.split(";");

            return indexExists(details, USER_EMAIL_INDEX) ? details[USER_EMAIL_INDEX].trim() : null;
        }
    }

    @Override
    public String getAuthorisedUserForename(HttpServletRequest request) {
        return getUserAttribute(request, USER_FORENAME_INDEX);
    }

    @Override
    public String getAuthorisedUserSurname(HttpServletRequest request) {
        return getUserAttribute(request, USER_SURNAME_INDEX);
    }

    @Override
    public String getAuthorisedScope(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_AUTHORISED_SCOPE);
    }

    @Override
    public String getAuthorisedRoles(HttpServletRequest request) {
        return getRequestHeader(request, ERIC_AUTHORISED_ROLES);
    }

    @Override
    public String[] getAuthorisedRolesArray(HttpServletRequest request) {
        String roles = getAuthorisedRoles(request);
        if (roles == null || roles.length() == 0) {
            return new String[0];
        }

        // roles are space separated list of authorized roles
        return roles.split(" ");
    }

    @Override
    public boolean isRoleAuthorised(HttpServletRequest request, String role) {
        if (role == null || role.length() == 0) {
            return false;
        }

        String[] roles = getAuthorisedRolesArray(request);
        if (roles.length == 0) {
            return false;
        }

        return ArrayUtils.contains(roles, role);
    }

    @Override
    public String[] getApiKeyPrivileges(HttpServletRequest request) {
        // Could be null if header is not present
        final String commaSeparatedPrivilegeString = request
                .getHeader(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER);

        return Optional.ofNullable(commaSeparatedPrivilegeString)
                .map(v -> v.split(","))
                .orElse(new String[]{});
    }

    @Override
    public boolean isKeyElevatedPrivilegesAuthorised(HttpServletRequest request) {
        String[] privileges = getApiKeyPrivileges(request);
        return request.getMethod().equals(PUT_METHOD) ? ArrayUtils.contains(privileges, INTERNAL_APP_PRIVILEGE) :
                ArrayUtils.contains(privileges, SENSITIVE_DATA_PRIVILEGE);
    }

    @Override
    public Map<String, List<String>> getTokenPermissions(HttpServletRequest request) {
        String tokenPermissionsHeader = request.getHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS_HEADER);

        Map<String, List<String>> permissions = new HashMap<>();

        if (tokenPermissionsHeader != null) {
            for (String pair : tokenPermissionsHeader.split(" ")) {
                String[] parts = pair.split("=");
                permissions.put(parts[0], Arrays.asList(parts[1].split(",")));
            }
        }

        return permissions;
    }

    @Override
    public boolean isTokenProtectedAndCompanyAuthorised(HttpServletRequest request, String companyNumber) {
        Map<String, List<String>> privileges = getTokenPermissions(request);

        return privileges.containsKey(COMPANY_OFFICER_PERMISSION) &&
                privileges.get(COMPANY_OFFICER_PERMISSION).contains(READ_PROTECTED) &&
                privileges.containsKey(COMPANY_NUMBER_PERMISSION) &&
                privileges.get(COMPANY_NUMBER_PERMISSION).contains(companyNumber);
    }

    private String getRequestHeader(HttpServletRequest request, String header) {
        return request == null ? null : request.getHeader(header);
    }

    private String getUserAttribute(final HttpServletRequest request, final int userAttributeIndex) {
        final String authorisedUser = getAuthorisedUser(request);

        if (authorisedUser == null || authorisedUser.trim().length() == 0) {
            return null;
        } else {
            final String[] details = authorisedUser.split(";");

            return indexExists(details, userAttributeIndex) ? getValue(details[userAttributeIndex].trim()) : null;
        }
    }

    private String getValue(String value) {
        return indexExists(value.split("="), 1) ? value.split("=")[1] : null;
    }

    private boolean indexExists(final String[] list, final int index) {
        return index >= 0 && index < list.length;
    }

}
