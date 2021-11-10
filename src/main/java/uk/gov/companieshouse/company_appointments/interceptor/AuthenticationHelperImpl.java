package uk.gov.companieshouse.company_appointments.interceptor;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
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
    public static final String INTERNAL_APP_PRIVILEGE = "internal-app";
    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";
    private static final String ERIC_AUTHORISED_SCOPE = "ERIC-Authorised-Scope";
    private static final String ERIC_AUTHORISED_ROLES = "ERIC-Authorised-Roles";
    public static final String ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER = "ERIC-Authorised-Key-Privileges";

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
        final String headerValue = request.getHeader(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER);
        if (headerValue == null) {
            return new String[]{};
        }


        return headerValue.split(",");
    }

    @Override
    public boolean isKeyElevatedPrivilegesAuthorised(HttpServletRequest request) {
        return ArrayUtils.contains(getApiKeyPrivileges(request), INTERNAL_APP_PRIVILEGE);
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
