package org.keycloak.protocol.oidc.grants.clientcredential;

import static org.keycloak.protocol.oidc.endpoints.TokenEndpoint.checkAndBindMtlsHoKToken;
import static org.keycloak.protocol.oidc.endpoints.TokenEndpoint.getRequestedScopes;
import static org.keycloak.protocol.oidc.endpoints.TokenEndpoint.updateUserSessionFromClientAuth;

import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.Urls;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.ServiceAccountTokenRequestContext;
import org.keycloak.services.clientpolicy.context.ServiceAccountTokenResponseContext;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.resources.Cors;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

public class ClientCredentialGrantType {

    private static final Logger logger = Logger.getLogger(ClientCredentialGrantType.class);

    private final ClientModel client;
    private final EventBuilder event;
    private final KeycloakSession session;
    private final Cors cors;
    private final MultivaluedMap<String, String> formParams;
    private final OIDCAdvancedConfigWrapper clientConfig;
    private final Map<String, String> clientAuthAttributes;
    private final TokenManager tokenManager;
    private UserSessionModel userSession;

    public ClientCredentialGrantType(ClientModel client, EventBuilder event, KeycloakSession session, Cors cors, MultivaluedMap<String, String> formParams, Map<String, String> clientAuthAttributes, TokenManager tokenManager) {
        this.client = client;
        this.clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(client);
        this.event = event;
        this.session = session;
        this.cors = cors;
        this.formParams = formParams;
        this.clientAuthAttributes = clientAuthAttributes;
        this.tokenManager = tokenManager;
    }

    public Response clientCredentialsGrant() {
        return clientCredentialsGrant(null);
    }

    public Response clientCredentialsGrant(UserModel user) {
        if (client.isBearerOnly()) {
            event.error(Errors.INVALID_CLIENT);
            throw new CorsErrorResponseException(cors, OAuthErrorException.UNAUTHORIZED_CLIENT, "Bearer-only client not allowed to retrieve service account", Response.Status.UNAUTHORIZED);
        }

        if (client.isPublicClient()) {
            event.error(Errors.INVALID_CLIENT);
            throw new CorsErrorResponseException(cors, OAuthErrorException.UNAUTHORIZED_CLIENT, "Public client not allowed to retrieve service account", Response.Status.UNAUTHORIZED);
        }

        UserModel clientUser;

        if (user == null) {
            if (!client.isServiceAccountsEnabled()) {
                event.error(Errors.INVALID_CLIENT);
                throw new CorsErrorResponseException(cors, OAuthErrorException.UNAUTHORIZED_CLIENT, "Client not enabled to retrieve service account", Response.Status.UNAUTHORIZED);
            }

            clientUser = session.users().getServiceAccount(client);

            if (clientUser == null || client.getProtocolMapperByName(OIDCLoginProtocol.LOGIN_PROTOCOL, ServiceAccountConstants.CLIENT_ID_PROTOCOL_MAPPER) == null) {
                // May need to handle bootstrap here as well
                logger.debugf("Service account user for client '%s' not found or default protocol mapper for service account not found. Creating now", client.getClientId());
                new ClientManager(new RealmManager(session)).enableServiceAccount(client);
                clientUser = session.users().getServiceAccount(client);
            }
        } else {
            clientUser = user;
        }

        String clientUsername = clientUser.getUsername();
        event.detail(Details.USERNAME, clientUsername);
        event.user(clientUser);

        if (!clientUser.isEnabled()) {
            event.error(Errors.USER_DISABLED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "User '" + clientUsername + "' disabled", Response.Status.UNAUTHORIZED);
        }

        String scope = getRequestedScopes(formParams, client, session, cors, event);
        RealmModel realm = session.getContext().getRealm();
        RootAuthenticationSessionModel rootAuthSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, false);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);

        authSession.setAuthenticatedUser(clientUser);
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        authSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, scope);

        // persisting of userSession by default
        UserSessionModel.SessionPersistenceState sessionPersistenceState = UserSessionModel.SessionPersistenceState.PERSISTENT;

        boolean useRefreshToken = clientConfig.isUseRefreshTokenForClientCredentialsGrant();
        if (!useRefreshToken) {
            // we don't want to store a session hence we mark it as transient, see KEYCLOAK-9551
            sessionPersistenceState = UserSessionModel.SessionPersistenceState.TRANSIENT;
        }

        ClientConnection clientConnection = session.getContext().getConnection();

        this.userSession = new UserSessionManager(session).createUserSession(authSession.getParentSession().getId(), realm, clientUser, clientUsername,
                clientConnection.getRemoteAddr(), ServiceAccountConstants.CLIENT_AUTH, false, null, null, sessionPersistenceState);
        event.session(userSession);

        AuthenticationManager.setClientScopesInSession(authSession);
        ClientSessionContext clientSessionCtx = TokenManager.attachAuthenticationSession(session, userSession, authSession);

        // Notes about client details
        userSession.setNote(ServiceAccountConstants.CLIENT_ID_SESSION_NOTE, client.getClientId()); // This is for backwards compatibility
        userSession.setNote(ServiceAccountConstants.CLIENT_ID, client.getClientId());
        userSession.setNote(ServiceAccountConstants.CLIENT_HOST, clientConnection.getRemoteHost());
        userSession.setNote(ServiceAccountConstants.CLIENT_ADDRESS, clientConnection.getRemoteAddr());

        try {
            session.clientPolicy().triggerOnEvent(new ServiceAccountTokenRequestContext(formParams, clientSessionCtx.getClientSession()));
        } catch (ClientPolicyException cpe) {
            event.error(cpe.getError());
            throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        updateUserSessionFromClientAuth(userSession, clientAuthAttributes);

        TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager.responseBuilder(realm, client, event, session, userSession, clientSessionCtx)
                .generateAccessToken();

        // Make refresh token generation optional, see KEYCLOAK-9551
        if (useRefreshToken) {
            responseBuilder = responseBuilder.generateRefreshToken();
        } else {
            responseBuilder.getAccessToken().setSessionState(null);
        }

        checkAndBindMtlsHoKToken(responseBuilder, useRefreshToken, session, clientConfig, cors, event);

        String scopeParam = clientSessionCtx.getClientSession().getNote(OAuth2Constants.SCOPE);
        if (TokenUtil.isOIDCRequest(scopeParam)) {
            responseBuilder.generateIDToken().generateAccessTokenHash();
        }

        try {
            session.clientPolicy().triggerOnEvent(new ServiceAccountTokenResponseContext(formParams, clientSessionCtx.getClientSession(), responseBuilder));
        } catch (ClientPolicyException cpe) {
            event.error(cpe.getError());
            throw new CorsErrorResponseException(cors, cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        // TODO : do the same as codeToToken()
        AccessTokenResponse res = null;
        try {
            res = responseBuilder.build();
        } catch (RuntimeException re) {
            if ("can not get encryption KEK".equals(re.getMessage())) {
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST,
                        "can not get encryption KEK", Response.Status.BAD_REQUEST);
            } else {
                throw re;
            }
        }
        event.success();

        return cors.builder(Response.ok(res, MediaType.APPLICATION_JSON_TYPE)).build();
    }

    public UserSessionModel getUserSession() {
        return userSession;
    }
}
