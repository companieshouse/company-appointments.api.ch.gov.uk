package uk.gov.companieshouse.company_appointments.interceptor;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for authenticating users
 */
public interface AuthenticationHelper {

    /**
     * Returns the authorised identify
     *
     * @param request the {@link HttpServletRequest}
     * @return the identify
     */
    String getAuthorisedIdentity(HttpServletRequest request);

    /**
     * Returns the authorised identity type
     *
     * @param request the {@link HttpServletRequest}
     * @return the identity type
     */
    String getAuthorisedIdentityType(HttpServletRequest request);

    /**
     * Verifies that the identity type is key
     *
     * @param identityType the identify type to be checked
     * @return true if the identity type is the key
     */
    boolean isApiKeyIdentityType(final String identityType);

    /**
     * Verifies that the identity type is Oauth2
     *
     * @param identityType the identity type to be checked
     * @return true if the identity type is Oauth2
     */
    boolean isOauth2IdentityType(final String identityType);

    /**
     * Returns the authorised user information
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised user
     */
    String getAuthorisedUser(HttpServletRequest request);

    /**
     * Returns the authorised user email
     *
     * @param request the {@link HttpServletRequest}
     * @return the user email
     */
    String getAuthorisedUserEmail(HttpServletRequest request);

    /**
     * Returns the authorised user forename
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised user forename
     */
    String getAuthorisedUserForename(HttpServletRequest request);

    /**
     * Returns the authorised user surname
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised user surname
     */
    String getAuthorisedUserSurname(HttpServletRequest request);

    /**
     * Returns the authorised scope
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised scope
     */
    String getAuthorisedScope(HttpServletRequest request);

    /**
     * Returns the authorised roles information
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised roles
     */
    String getAuthorisedRoles(HttpServletRequest request);

    /**
     * Returns an array of the authorised roles
     *
     * @param request the {@link HttpServletRequest}
     * @return the authorised roles
     */
    String[] getAuthorisedRolesArray(HttpServletRequest request);

    /**
     * Checks whether the specified role has authorisation
     *
     * @param request the {@link HttpServletRequest}
     * @param role    the role to be checked
     * @return true if the role is authorised
     */
    boolean isRoleAuthorised(HttpServletRequest request, String role);

    /**
     * Returns the privileges granted to the API key
     *
     * @param request the {@link HttpServletRequest}
     * @return the privileges of the API key
     */
    String[] getApiKeyPrivileges(HttpServletRequest request);

    /**
     * Checks whether the key has elevated privileges
     *
     * @param request the {@link HttpServletRequest}
     * @return true if the key has elevated privileges
     */
    boolean isKeyElevatedPrivilegesAuthorised(HttpServletRequest request);

    /**
     * Returns the permissions granted to the OAuth2 Token
     *
     * @param request the {@link HttpServletRequest}
     * @return the privileges of the OAuth2 Token
     */
    Map<String, List<String>> getTokenPermissions(HttpServletRequest request);

    /**
     * Checks whether the token has required permissions
     *
     * @param request the {@link HttpServletRequest}
     * @return true if the token has required permissions
     */
    boolean isTokenProtectedAndCompanyAuthorised(HttpServletRequest request, String companyNumber);
}
